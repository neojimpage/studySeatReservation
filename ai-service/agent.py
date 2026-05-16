from langchain_deepseek import ChatDeepSeek
from langgraph.prebuilt import create_react_agent
from langchain_community.chat_message_histories import SQLChatMessageHistory
from langchain_core.messages import HumanMessage, AIMessage

from config import DEEPSEEK_API_KEY, DEEPSEEK_MODEL
from tools import (search_seats, create_reservation, cancel_reservation,
                    get_my_reservations, get_my_profile)


SYSTEM_PROMPT = """你是校园自习助手，帮助学生预约自习座位和规划学习计划。
先理解用户意图，必要时调用工具获取信息，最后用自然语言回复用户。

规则：
- 预约前必须确认日期、时段、区域/座位偏好
- 创建预约前需用户明确确认
- 自习计划规划时先查可用座位，以表格呈现计划
- 批量预约逐个创建，失败时说明原因
- 回复简洁友好，使用中文"""


def get_session_history(session_id: str) -> SQLChatMessageHistory:
    return SQLChatMessageHistory(
        session_id=session_id,
        connection="sqlite:///chat_history.db"
    )


def create_agent():
    llm = ChatDeepSeek(
        model=DEEPSEEK_MODEL,
        api_key=DEEPSEEK_API_KEY,
        temperature=0.7,
    )

    tools = [
        search_seats,
        create_reservation,
        cancel_reservation,
        get_my_reservations,
        get_my_profile,
    ]

    return create_react_agent(llm, tools, prompt=SYSTEM_PROMPT)


def chat_with_memory(agent, user_message: str, session_id: str) -> str:
    """Send a message to the agent with full conversation history.

    Memory strategy:
    - SQLChatMessageHistory stores only clean user+AI message pairs in SQLite
    - Each turn: load all past messages -> append new user message -> invoke agent
    - After agent responds: save only the new user+AI pair to history
    - Tool calls and internal agent messages are NOT persisted (they'd confuse the next turn)
    """
    history = get_session_history(session_id)

    # Load past conversation (only clean user/AI pairs, no tool messages)
    past_messages = list(history.messages)

    # Build full input: past conversation + new user message
    input_messages = past_messages + [HumanMessage(content=user_message)]

    # Invoke agent with full context
    result = agent.invoke({"messages": input_messages})

    # Extract only the final AI response (last message in agent output)
    ai_response = result["messages"][-1].content

    # Save ONLY the new user+AI pair to persistent history
    # Never save tool messages — they cause the agent to re-process old tool calls
    history.add_message(HumanMessage(content=user_message))
    history.add_message(AIMessage(content=ai_response))

    return ai_response
