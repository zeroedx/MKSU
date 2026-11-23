import asyncio
import os
import sys

from telethon import TelegramClient

API_ID = 611335
API_HASH = "d524b414d21f4d37f08684c1df41ac9c"

BOT_TOKEN = os.environ.get("BOT_TOKEN")
CHAT_ID = os.environ.get("CHAT_ID")
MESSAGE_THREAD_ID = os.environ.get("MESSAGE_THREAD_ID")
TITLE = os.environ.get("TITLE", "MKSU Manager")
VERSION = os.environ.get("VERSION")
BRANCH = os.environ.get("BRANCH", "main")
RUN_URL = os.environ.get("RUN_URL")
COMPARE_URL = os.environ.get("COMPARE_URL")

MSG_TEMPLATE = """{title}
分支：{branch}
#ci_{version}
[提交对比]({compare_url})
[构建记录]({run_url})"""


def get_compare_url() -> str:
    if COMPARE_URL:
        return COMPARE_URL
    before = os.environ.get("BEFORE_SHA", "")
    after = os.environ.get("AFTER_SHA", "")
    repo = os.environ.get("GITHUB_REPOSITORY", "")
    if before and after and repo:
        return f"https://github.com/{repo}/compare/{before}...{after}"
    return ""


def get_caption() -> str:
    return MSG_TEMPLATE.format(
        title=TITLE,
        branch=BRANCH,
        version=VERSION,
        compare_url=get_compare_url(),
        run_url=RUN_URL,
    )


def check_environ() -> None:
    global CHAT_ID, MESSAGE_THREAD_ID
    if not BOT_TOKEN:
        print("[-] Invalid BOT_TOKEN")
        sys.exit(1)
    if not CHAT_ID:
        print("[-] Invalid CHAT_ID")
        sys.exit(1)
    try:
        CHAT_ID = int(CHAT_ID)
    except ValueError:
        pass
    if not RUN_URL:
        print("[-] Invalid RUN_URL")
        sys.exit(1)
    if not VERSION:
        print("[-] Invalid VERSION")
        sys.exit(1)
    if not BRANCH:
        print("[-] Invalid BRANCH")
        sys.exit(1)
    if MESSAGE_THREAD_ID:
        try:
            MESSAGE_THREAD_ID = int(MESSAGE_THREAD_ID)
        except ValueError:
            print("[-] Invalid MESSAGE_THREAD_ID")
            sys.exit(1)
    else:
        MESSAGE_THREAD_ID = None


def record_message_id(message_id: int) -> None:
    print(f"[+] Telegram message id: {message_id}")
    output_path = os.environ.get("GITHUB_OUTPUT")
    if output_path:
        with open(output_path, "a", encoding="utf-8") as fh:
            fh.write(f"message_id={message_id}\n")
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as fh:
            fh.write(f"Telegram message id: {message_id}\n")


async def main() -> int:
    check_environ()
    files = sys.argv[1:]
    if not files:
        print("[-] No files to upload")
        return 1

    print("[+] Uploading to telegram:", ", ".join(os.path.basename(f) for f in files))
    caption = [""] * len(files)
    caption[-1] = get_caption()
    print("[+] Caption:")
    print("---")
    print(caption[-1])
    print("---")

    script_dir = os.path.dirname(os.path.abspath(sys.argv[0]))
    session_dir = os.path.join(script_dir, "ksubot")
    async with await TelegramClient(session=session_dir, api_id=API_ID, api_hash=API_HASH).start(bot_token=BOT_TOKEN) as bot:
        sent = await bot.send_file(
            entity=CHAT_ID,
            file=files,
            caption=caption,
            reply_to=MESSAGE_THREAD_ID,
            parse_mode="markdown",
        )

    message = sent[0] if isinstance(sent, list) else sent
    print(f"[+] Telegram upload succeeded: message id {message.id} in {CHAT_ID}")
    record_message_id(message.id)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(asyncio.run(main()))
    except Exception as e:
        print(f"[-] Telegram upload failed: {e}")
        sys.exit(1)
