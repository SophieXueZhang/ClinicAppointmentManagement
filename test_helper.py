import json
import sys

def parse_token(json_file):
    with open(json_file, 'r') as f:
        data = json.load(f)
        return data['access_token']

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Usage: python test_helper.py <json_file>")
        sys.exit(1)
    
    token = parse_token(sys.argv[1])
    print(token)
