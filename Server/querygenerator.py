import medicalconst as mc

class QueryGenerator:
    def __init__(self):
        self.item_limit = 25 # the maximum number of items
    
    '''generate query statement based on question_meta'''
    def generate_query(self, question_meta):
        if question_meta == {} or question_meta['question_types'] == []:
            return []
        type_entity = self.get_question_type_entity(question_meta['args'])
        question_types = question_meta['question_types']
        query_material = {}
        for ques_type in question_types:
            query_material[ques_type] = type_entity.get(ques_type.split('_')[0])
        CQLs = []
        for ques_type in question_types:
            cql_dict = {}
            cql_dict['question_type'] = ques_type
            cql = self.cql_generator(ques_type, query_material[ques_type])
            if cql:
                cql_dict['cql'] = cql
                CQLs.append(cql_dict)
        return CQLs
    
    '''generate the Cypher Query Language statements'''
    def cql_generator(self, question_type, entities):
        if question_type == '' or entities == []:
            return []

        # Cypher query language statements
        cql = []
        # symptom - disease
        if question_type == mc.SYMP_DISE:
            cql = ["MATCH (m:Disease)-[r:has_symptom]->(n:Symptom) WHERE n.name = '{0}' RETURN m.name, r.name, n.name LIMIT {1}"
                .format(i, self.item_limit) for i in entities]
        # disease - symptom
        elif question_type == mc.DISE_SYMP:
            cql = ["MATCH (m:Disease)-[r:has_symptom]->(n:Symptom) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
        # disease - department
        elif question_type == mc.DISE_DEPART:
            cql = ["MATCH (m:Disease)-[r:belongs_to]->(n:Department) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
        # disease - check
        elif question_type == mc.DISE_CHECK:
            cql = ["MATCH (m:Disease)-[r:need_check]->(n:Check) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
        # check - disease
        elif question_type == mc.CHECK_DISE:
            cql = ["MATCH (m:Disease)-[r:need_check]->(n:Check) WHERE n.name = '{0}' RETURN m.name, r.name, n.name LIMIT {1}"
                .format(i, self.item_limit) for i in entities]
        # disease - cause
        elif question_type == mc.DISE_CAUSE:
            cql = ["MATCH (m:Disease) WHERE m.name = '{0}' RETURN m.name, m.cause".format(i) for i in entities]
        # disease - do - food
        elif question_type == mc.DISE_DO_FOOD:
            cql1 = ["MATCH (m:Disease)-[r:recommand_eat]->(n:Food) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
            cql2 = ["MATCH (m:Disease)-[r:do_eat]->(n:Food) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
            cql = cql1 + cql2
        # disease - not -food
        elif question_type == mc.DISE_NOT_FOOD:
            cql = ["MATCH (m:Disease)-[r:not_eat]->(n:Food) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
        # disease - drug
        elif question_type == mc.DISE_DRUG:
            cql1 = ["MATCH (m:Disease)-[r:common_drug]->(n:Drug) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
            cql2 = ["MATCH (m:Disease)-[r:recommand_drug]->(n:Drug) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
            cql = cql1 + cql2
        # drug -disease
        elif question_type == mc.DRUG_DISE:
            cql1 = ["MATCH (m:Disease)-[r:common_drug]->(n:Drug) WHERE n.name = '{0}' RETURN m.name, r.name, n.name LIMIT {1}"
                .format(i, self.item_limit) for i in entities]
            cql2 = ["MATCH (m:Disease)-[r:recommand_drug]->(n:Drug) WHERE n.name = '{0}' RETURN m.name, r.name, n.name LIMIT {1}"
                .format(i, self.item_limit) for i in entities]
            cql = cql1 + cql2
        # disease - cureway
        elif question_type == mc.DISE_CUREWAY:
            cql = ["MATCH (m:Disease)-[r:cure_way]->(n:Cure) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
        # disease - cureprob
        elif question_type == mc.DISE_CUREPROB:
            cql = ["MATCH (m:Disease) WHERE m.name = '{0}' RETURN m.name, m.cured_prob".format(i) for i in entities]
        # disease - complication
        elif question_type == mc.DISE_COMP:
            cql = ["MATCH (m:Disease)-[r:acompany_with]->(n:Disease) WHERE m.name = '{0}' RETURN m.name, r.name, n.name"
                .format(i) for i in entities]
        # disease - prevent
        elif question_type == mc.DISE_PREV:
            cql = ["MATCH (m:Disease) WHERE m.name = '{0}' RETURN m.name, m.prevent".format(i) for i in entities]
        # disease - duration
        elif question_type == mc.DISE_DURA:
            cql = ["MATCH (m:Disease) WHERE m.name = '{0}' RETURN m.name, m.cure_lasttime".format(i) for i in entities]
        # disease - easyget
        elif question_type == mc.DISE_EASY:
            cql = ["MATCH (m:Disease) WHERE m.name = '{0}' RETURN m.name, m.easy_get".format(i) for i in entities]
        # disease - description
        elif question_type == mc.DISE_DESC:
            cql = ["MATCH (m:Disease) WHERE m.name = '{0}' RETURN m.name, m.desc".format(i) for i in entities]
        return cql

    '''get the types and entities in question'''
    def get_question_type_entity(self, meta_args):
        type_entity = {}
        for entity, types in meta_args.items():
            for type in types:
                if type in type_entity:
                    type_entity[type].append(entity)
                else:
                    type_entity[type] = [entity]
        return type_entity

