from fastapi import APIRouter, Request
from fastapi.responses import HTMLResponse
import secrets
import urllib.parse
import time
import hashlib
import asyncio
import html
from app.config import settings
from app.core.telegram_client import TelegramClient
from app.core.gateway_client import GatewayClient
from app.core.logging_auth import logger

router = APIRouter()

state_storage = {}

telegram_client = TelegramClient()
gateway_client = GatewayClient()


@router.get("/login")
async def login():
    """
    Шаг 1: Инициируем вход через Telegram
    """
    state = secrets.token_urlsafe(32)

    state_storage[state] = {
        "created_at": time.time(),
        "used": False
    }

    logger.info(f"=== NEW LOGIN ATTEMPT ===")
    logger.info(f"Generated state: {state[:20]}...")

    params = {
        "client_id": settings.telegram_client_id,
        "redirect_uri": settings.telegram_redirect_uri,
        "response_type": "code",
        "scope": "openid profile",
        "state": state
    }

    telegram_url = f"https://oauth.telegram.org/auth?{urllib.parse.urlencode(params)}"
    logger.info(f"Telegram URL generated")
    logger.info(f"=== LOGIN ATTEMPT COMPLETE ===\n")

    return {
        "auth_url": telegram_url,
        "state": state
    }


@router.get("/callback")
async def callback(request: Request):
    """
    Шаг 2: Telegram перенаправляет пользователя сюда с кодом
    """
    code = request.query_params.get("code")
    state = request.query_params.get("state")

    logger.info(f"=== CALLBACK RECEIVED ===")
    logger.info(f"Code: {code[:20] if code else 'None'}...")
    logger.info(f"State: {state[:20] if state else 'None'}...")

    if not code or not state:
        logger.error("Missing code or state")
        return {
            "success": False,
            "error": "missing_params",
            "details": "Missing code or state"
        }

    if state not in state_storage:
        logger.error(f"Invalid state: {state[:20]}... not found in storage")
        return {
            "success": False,
            "error": "invalid_state",
            "details": "State not found or expired"
        }

    state_info = state_storage[state]
    if state_info["used"]:
        logger.error(f"State already used: {state[:20]}...")
        return {
            "success": False,
            "error": "state_used",
            "details": "State already used"
        }

    logger.info(f"State validated successfully")

    state_storage[state]["used"] = True

    current_time = time.time()
    expired_states = [s for s, info in state_storage.items()
                      if current_time - info["created_at"] > 300]
    for s in expired_states:
        logger.info(f"Cleaning up expired state: {s[:20]}...")
        del state_storage[s]

    try:
        logger.info("Exchanging code for ID token...")
        id_token = await telegram_client.exchange_code(code)
        logger.info("✓ Code exchanged successfully")

        logger.info("Verifying ID token...")
        user_data = await telegram_client.verify_and_decode_id_token(id_token)
        logger.info(f"✓ Token verified. User: {user_data.get('name')} (ID: {user_data['telegram_id']})")

        telegram_id = user_data["telegram_id"]
        hash_input = f"{telegram_id}{settings.hash_salt}"
        user_hash = hashlib.sha256(hash_input.encode()).hexdigest()
        logger.info(f"✓ Hash generated: {user_hash[:20]}...")

        logger.info("Starting Gateway sync task...")
        sync_success = await gateway_client.sync_user_hash(user_hash, user_data.get("name"))
        if not sync_success:
            logger.error(f"❌ Authentication error: {str(e)}")
            logger.info(f"=== AUTHENTICATION FAILED ===\n")
            return {
                "success": False,
                "error": "auth_failed",
                "details": "bruh"
            }

        logger.info(f"=== AUTHENTICATION SUCCESSFUL ===\n")

        username = user_data.get("name") or "Telegram-пользователь"

        # Возвращаем пользователя в мобильное приложение через deep link.
        # Передаём НАСТОЯЩИЕ user_hash и имя — приложение их сохранит и покажет
        # в профиле. Используем HTML-страницу с авто-редиректом + кнопкой,
        # т.к. часть браузеров блокирует автоматический переход на кастомную
        # схему (vkusnyvybor://) и требует клика пользователя.
        deep_link = (
            "vkusnyvybor://auth/callback?"
            + urllib.parse.urlencode({
                "success": "true",
                "user_hash": user_hash,
                "username": username,
            })
        )
        safe_link = html.escape(deep_link, quote=True)
        safe_name = html.escape(username)

        page = f"""<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Вход выполнен</title>
  <meta http-equiv="refresh" content="0; url={safe_link}">
  <style>
    body {{ font-family: -apple-system, Roboto, sans-serif; background:#f4f6f9;
           display:flex; min-height:100vh; margin:0; align-items:center;
           justify-content:center; }}
    .card {{ background:#fff; padding:40px; border-radius:12px; text-align:center;
            box-shadow:0 4px 20px rgba(0,0,0,.08); max-width:360px; }}
    h2 {{ color:#333; margin:0 0 8px; }}
    p {{ color:#666; font-size:14px; }}
    a.btn {{ display:inline-block; margin-top:20px; background:#54a9eb; color:#fff;
            padding:12px 24px; border-radius:8px; font-weight:bold;
            text-decoration:none; }}
  </style>
  <script>
    // Подстраховка к meta-refresh для браузеров, где он не срабатывает.
    window.location.href = "{safe_link}";
  </script>
</head>
<body>
  <div class="card">
    <h2>Вход выполнен ✅</h2>
    <p>Добро пожаловать, {safe_name}!<br>Возвращаемся в приложение…</p>
    <a class="btn" href="{safe_link}">Открыть приложение</a>
  </div>
</body>
</html>"""

        return HTMLResponse(content=page)

    except Exception as e:
        logger.error(f"❌ Authentication error: {str(e)}")
        logger.info(f"=== AUTHENTICATION FAILED ===\n")
        return {
            "success": False,
            "error": "auth_failed",
            "details": str(e)
        }