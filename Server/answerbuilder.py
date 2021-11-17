from typing_extensions import final
from py2neo import Graph
import medicalconst as mc

class AnswerBuilder:
    def __init__(self):
        self.kg = Graph(
            "http://localhost:7474",
            user="neo4j",
            password="TM2345678901"
        )
        self.item_limit = 25

    '''generate the final answers'''
    def build_answer(self, cqls):
        final_answers = []
        for cql in cqls:
            ques_type = cql['question_type']
            queries = cql['cql']
            for query in queries:
                ans = self.kg.run(query).data()
                formatting_answer = self.answer_formatter(ques_type, ans)
                if formatting_answer:
                    final_answers.append(formatting_answer.strip())
        return final_answers

    '''fomatting the answer based on their question type'''
    def answer_formatter(self, ques_type, answer):
        if not answer:
            return ''
        formatting_answer = ''
        if ques_type == mc.SYMP_DISE:
            result = [a['m.name'] for a in answer]
            object = answer[0]['n.name']
            formatting_answer = '与{0}相关的疾病有{1}。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_SYMP:
            result = [a['n.name'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}可能引起的症状有{1}。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_DEPART:
            result = [a['n.name'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}属于{1}。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_CHECK:
            result = [a['n.name'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}建议做如下的检查：{1}。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.CHECK_DISE:
            result = [a['m.name'] for a in answer]
            object = answer[0]['n.name']
            formatting_answer = '{0}可以用来检查{1}等疾病。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_CAUSE:
            result = [a['m.cause'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}的病因有{1}。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_DO_FOOD:
            result = [a['n.name'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}建议多吃{1}等食物。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_NOT_FOOD:
            result = [a['n.name'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}不宜吃{1}等食物。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_DRUG:
            result = [a['n.name'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}等药品对{1}有治疗作用。本建议仅供参考，具体用药切记遵从医嘱。'.format(
                 '，'.join(list(set(result))), object)
        elif ques_type == mc.DRUG_DISE:
            result = [a['m.name'] for a in answer]
            object = answer[0]['n.name']
            formatting_answer = '{0}可以用来治疗{1}等疾病。本建议仅供参考，具体用药切记遵从医嘱。'.format(object, 
                '，'.join(list(set(result)))) 
        elif ques_type == mc.DISE_CUREWAY:
            result = [a['n.name'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}可以采取以下治疗方式：{1}。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_CUREPROB:
            result = answer[0]['m.cured_prob']
            object = answer[0]['m.name']
            formatting_answer = '{0}的治愈率为{1}。'.format(object, result)
        elif ques_type == mc.DISE_COMP:
            result = [a['n.name'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}的并发症有{1}。'.format(object, '，'.join(list(set(result))))
        elif ques_type == mc.DISE_PREV:
            result = [a['m.prevent'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}的预防措施有：\n{1}。'.format(object, '，'.join(result))
        elif ques_type == mc.DISE_DURA:
            result = answer[0]['m.cure_lasttime']
            object = answer[0]['m.name']
            formatting_answer = '{0}的治疗周期一般{1}。'.format(object, result)
        elif ques_type == mc.DISE_EASY:
            result = [a['m.easy_get'] for a in answer]
            object = answer[0]['m.name']
            formatting_answer = '{0}的易感人群：{1}。'.format(object, '，'.join(result))
        elif ques_type == mc.DISE_DESC:
            result = answer[0]['m.desc']
            formatting_answer = '{0}'.format(result)
        return formatting_answer

