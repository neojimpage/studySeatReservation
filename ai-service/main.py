from fastapi import FastAPI
from pydantic import BaseModel
from agent import create_agent, chat_with_memory
from tools import current_user_id

app = FastAPI(title="StudySeat AI Service")

agent = create_agent()


class ChatRequest(BaseModel):
    message: str
    conversationId: str
    userId: str


class ChatResponse(BaseModel):
    reply: str
    conversationId: str


@app.post("/ai/chat")
async def chat(req: ChatRequest) -> ChatResponse:
    # Set user context so tools know which user is calling
    current_user_id.set(req.userId)

    reply = chat_with_memory(agent, req.message, req.conversationId)
    return ChatResponse(reply=reply, conversationId=req.conversationId)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
