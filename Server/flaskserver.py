'''
The medical QA server, based on Flask framework.
'''
from gevent import monkey
from gevent.pywsgi import WSGIServer
monkey.patch_all()
from flask import Flask, make_response, request
import json
import os 
import time
from questionparser import QuestionParser
from querygenerator import QueryGenerator
from answerbuilder import AnswerBuilder

app = Flask(__name__)
parser = QuestionParser()
query_generator = QueryGenerator()
answer_builder = AnswerBuilder()
entities_dict = dict()
print('Server prepares well.')

'''the medical QA api'''
@app.route('/question', methods=['POST'])
def question_and_answering():
    answers = []
    ip = request.remote_addr
    if request.method == 'POST':
        data = request.get_data()
        question_dict = json.loads(data)
        mac = question_dict['mac']
        user_id = ip + '$' + mac
        question = question_dict['question']
        question_meta = parser.parse_question(question, entities_dict, user_id)
        CQLs = query_generator.generate_query(question_meta)
        answers = answer_builder.build_answer(CQLs)

    if answers == []:
        answers = ['抱歉，暂时还无法解答你的问题，如需获取更多信息请咨询相关医生。']
    
    answers_dict = {'answers': answers}
    return json.dumps(answers_dict)

'''the feedback api'''
@app.route('/feedback', method=['POST'])
def feedback():
    prefix_path = '/'.join(os.path.abspath(__file__).split('/')[:-1])
    feedback_file_path = os.path.join(prefix_path, 'feedback')
    if not feedback_file_path:
        os.makedirs(feedback_file_path)

    question = ''
    feedback = ''
    if request.method == 'POST':
        data = request.get_data()
        data = json.loads(data)
        feedback = data['feedback']
        question = data['question']
    if question == '' or feedback == '':
        return

    ip = request.remote_addr
    mac = data['mac']
    user_id = ip + '$' + mac
    context_dict = entities_dict.get(user_id, default={})
    context = str(context_dict)

    curr_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
    
    fb =curr_time + '---' + user_id + '---' + feedback + '---' + question + '---' + context + '\n'

    with open(feedback_file_path+'/user_feedback.txt', 'a') as ff:
        ff.write(fb)
    

if __name__ == '__main__':
    WSGIServer(('0.0.0.0', 5320), app).serve_forever()
