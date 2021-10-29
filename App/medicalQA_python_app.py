import json
import requests
import uuid

def get_mac_address(): 
    mac=uuid.UUID(int = uuid.getnode()).hex[-12:] 
    return ":".join([mac[e:e+2] for e in range(0,11,2)])
 
headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.63 Safari/537.36', 
    'Content-Type': 'application/json'
}
qa_url = 'http://106.15.90.138:5320/question'
local_mac_addr = get_mac_address()

while True:
    question = input("\033[33mquestion: \033[0m")
    question_json = json.dumps({'mac': local_mac_addr, 'question': question})
    response = requests.post(url=qa_url, headers=headers, data=question_json)
    answers = response.json()['answers']
    print('\033[32manswer:\033[0m')
    for ans in answers:
        if ans.strip():
            print(ans.strip())

