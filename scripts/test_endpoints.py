import requests
import json
from typing import Dict
from datetime import datetime, timedelta

BASE_URL = "http://localhost:8000"

def get_auth_token(username: str, password: str) -> str:
    """Get authentication token"""
    response = requests.post(
        f"{BASE_URL}/operators/token",
        data={"username": username, "password": password},
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    return response.json()["access_token"]

def create_group_ticket(token: str) -> Dict:
    """Create a group ticket type"""
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    data = {
        "name": "团体票",
        "price": 80.0,
        "description": "10人及以上团体使用",
        "min_group_size": 10,
        "status": True
    }
    response = requests.post(f"{BASE_URL}/ticket-types/", headers=headers, json=data)
    print(f"\nCreate group ticket response: {response.status_code}")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    return response.json()

def test_group_ticket_sales(token: str, ticket_type_id: int):
    """Test group ticket sales validation"""
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    # Test with invalid group size (should fail)
    invalid_data = {
        "ticket_type_id": ticket_type_id,
        "quantity": 5,  # Less than minimum
        "total_amount": 400.0,
        "operator_id": 1
    }
    response = requests.post(f"{BASE_URL}/sales/", headers=headers, json=invalid_data)
    print(f"\nInvalid group size test response: {response.status_code}")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    
    # Test with valid group size (should succeed)
    valid_data = {
        "ticket_type_id": ticket_type_id,
        "quantity": 15,  # More than minimum
        "total_amount": 1200.0,
        "operator_id": 1
    }
    response = requests.post(f"{BASE_URL}/sales/", headers=headers, json=valid_data)
    print(f"\nValid group size test response: {response.status_code}")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))

def test_statistics(token: str):
    """Test statistics endpoints"""
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    # Test daily statistics
    today = datetime.now().strftime("%Y-%m-%d")
    response = requests.get(f"{BASE_URL}/sales/daily-stats/{today}", headers=headers)
    print(f"\nDaily statistics response: {response.status_code}")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    
    # Test monthly statistics
    current_date = datetime.now()
    response = requests.get(
        f"{BASE_URL}/sales/stats/monthly/{current_date.year}/{current_date.month}", 
        headers=headers
    )
    print(f"\nMonthly statistics response: {response.status_code}")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))

def main():
    # Get authentication token
    token = get_auth_token("admin", "admin123")
    print(f"Got authentication token: {token[:20]}...")
    
    # Create group ticket type
    ticket_type = create_group_ticket(token)
    
    # Test group ticket sales validation
    test_group_ticket_sales(token, ticket_type["id"])
    
    # Test statistics endpoints
    test_statistics(token)

if __name__ == "__main__":
    main()
