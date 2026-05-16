import os
from dotenv import load_dotenv

load_dotenv()

# Disable LangSmith tracing (not needed for this project)
os.environ["LANGCHAIN_TRACING_V2"] = "false"
os.environ["LANGCHAIN_ENDPOINT"] = ""
os.environ["LANGCHAIN_API_KEY"] = ""
os.environ["LANGCHAIN_PROJECT"] = ""

DEEPSEEK_API_KEY = os.environ.get("DEEPSEEK_API_KEY", "your-api-key")
DEEPSEEK_MODEL = os.environ.get("DEEPSEEK_MODEL", "deepseek-chat")
JAVA_BASE_URL = os.environ.get("JAVA_BASE_URL", "http://localhost:8080")
