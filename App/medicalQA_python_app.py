import json
import requests

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.63 Safari/537.36', 
    'Content-Type': 'application/json'
}
qa_url = 'http://106.15.90.138:5000/question'


while True:
    question = input("question: ")
    question_json = json.dumps({'question': question})
    response = requests.post(url=qa_url, headers=headers, data=question_json)
    answers = response.json()['answers']
    print('\033[32manswer:\033[0m')
    for ans in answers:
        if ans.strip():
            print(ans.strip())

