import httpx
from contextvars import ContextVar
from langchain.tools import tool
from config import JAVA_BASE_URL

current_user_id: ContextVar[str] = ContextVar("current_user_id", default="")


def _uid() -> str:
    return current_user_id.get()


def _java_get(path: str, params: dict = None) -> dict:
    url = f"{JAVA_BASE_URL}{path}"
    if params is None:
        params = {}
    params["userId"] = _uid()
    resp = httpx.get(url, params=params, timeout=10)
    resp.raise_for_status()
    return resp.json()


def _java_post(path: str, data: dict = None) -> dict:
    url = f"{JAVA_BASE_URL}{path}"
    if data is None:
        data = {}
    data["userId"] = _uid()
    resp = httpx.post(url, data=data, timeout=10)
    resp.raise_for_status()
    return resp.json()


@tool
def search_seats(date: str, start_time: str, end_time: str,
                  area: str = None) -> str:
    """
    Query available seats for a given date and time range.
    date: date string like '2026-05-17'
    start_time: start time like '14:00'
    end_time: end time like '16:00'
    area: optional area name or ID filter
    """
    try:
        params = {}
        if area:
            params["areaId"] = area
        result = _java_get("/api/student/seats", params)
        if result.get("code") == 200:
            seats = result.get("data", [])
            available = [s for s in seats if s.get("status") == "空闲" and s.get("isEnabled")]
            if not available:
                return "未找到可用座位"
            lines = ["找到以下空闲座位："]
            for s in available:
                lines.append(f"  - {s['seatNumber']} (ID: {s['id']})")
            return "\n".join(lines)
        return f"查询失败: {result.get('message')}"
    except Exception as e:
        return f"查询座位出错: {str(e)}"


@tool
def create_reservation(seat_id: int, start_time: str, end_time: str) -> str:
    """
    Create a reservation for a specific seat and time.
    seat_id: the seat ID to reserve
    start_time: datetime string like '2026-05-17T14:00:00'
    end_time: datetime string like '2026-05-17T16:00:00'
    """
    try:
        result = _java_post("/api/student/reservations", {
            "seatId": seat_id,
            "startTime": start_time,
            "endTime": end_time,
        })
        if result.get("code") == 200:
            return f"预约成功！预约ID: {result['data']['id']}"
        return f"预约失败: {result.get('message')}"
    except Exception as e:
        return f"创建预约出错: {str(e)}"


@tool
def cancel_reservation(reservation_id: int) -> str:
    """Cancel an existing reservation by its ID."""
    try:
        result = _java_post(f"/api/student/reservations/{reservation_id}/cancel")
        if result.get("code") == 200:
            return "取消成功"
        return f"取消失败: {result.get('message')}"
    except Exception as e:
        return f"取消预约出错: {str(e)}"


@tool
def get_my_reservations() -> str:
    """Get the current user's reservation list."""
    try:
        result = _java_get("/api/student/reservations")
        if result.get("code") == 200:
            reservations = result.get("data", [])
            if not reservations:
                return "你当前没有预约记录"
            lines = ["你的预约记录："]
            for r in reservations:
                lines.append(
                    f"  - ID:{r['id']} 座位{r.get('seatId')} "
                    f"{r['startTime']}~{r['endTime']} 状态:{r['status']}"
                )
            return "\n".join(lines)
        return f"查询失败: {result.get('message')}"
    except Exception as e:
        return f"查询预约出错: {str(e)}"


@tool
def get_my_profile() -> str:
    """Get the current user's profile information."""
    try:
        result = _java_get(f"/api/student/user/{_uid()}")
        if result.get("code") == 200:
            u = result.get("data", {})
            return (
                f"学号: {u.get('studentId')}\n"
                f"姓名: {u.get('name')}\n"
                f"违规次数: {u.get('violationCount', 0)}\n"
                f"账号状态: {'已限制' if u.get('isRestricted') else '正常'}"
            )
        return f"查询失败: {result.get('message')}"
    except Exception as e:
        return f"查询个人信息出错: {str(e)}"
