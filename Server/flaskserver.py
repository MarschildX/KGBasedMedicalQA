'''
The medical QA server, based on Flask framework.
'''
from gevent import monkey
from gevent.pywsgi import WSGIServer
monkey.patch_all()
from flask import Flask, make_response, request
import json
from questionparser import QuestionParser
from querygenerator import QueryGenerator
from answerbuilder import AnswerBuilder

app = Flask(__name__)
parser = QuestionParser()
query_generator = QueryGenerator()
answer_builder = AnswerBuilder()
print('Server prepares well.')

'''the medical QA api'''
@app.route('/question', methods=['POST'])
def question_and_answering():
    answers = []
    ip = request.remote_addr
    if request.method == 'POST':
        data = request.get_data()
        question_dict = json.loads(data)
        question = question_dict['question']
        question_meta = parser.parse_question(question)
        CQLs = query_generator.generate_query(question_meta)
        answers = answer_builder.build_answer(CQLs)

    if answers == []:
        answers = ['抱歉，暂时还无法解答你的问题，如需获取更多信息请咨询相关医生。']
    
    answers_dict = {'answers': answers}
    return json.dumps(answers_dict)

if __name__ == '__main__':
    WSGIServer(('0.0.0.0', 5320), app).serve_forever()
