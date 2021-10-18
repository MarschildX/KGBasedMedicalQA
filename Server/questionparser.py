import ahocorasick
import os
import medicalconst as mc
import json


class QuestionParser:
    def __init__(self) -> None:
        # the prefix path of entity data
        self.prefix_path = '/'.join(os.path.abspath(__file__).split('/')[:-2])
        self.prefix_path = os.path.join(self.prefix_path, 'KnowledgeGraph/data')

        # the absolute path of entity data
        self.path_disease = os.path.join(self.prefix_path, 'disease.txt')
        self.path_symptom = os.path.join(self.prefix_path, 'symptoms.txt')
        self.path_department = os.path.join(self.prefix_path, 'department.txt')
        self.path_check = os.path.join(self.prefix_path, 'check.txt')
        self.path_food = os.path.join(self.prefix_path, 'food.txt')
        self.path_drug = os.path.join(self.prefix_path, 'drug.txt')
        self.path_producer = os.path.join(self.prefix_path, 'producer.txt')
        self.path_question_words = os.path.join(self.prefix_path, 'question_words.json')

        # load the entity data
        self.words_disease = [entity.strip() for entity in open(self.path_disease) if entity.strip()]
        self.words_symptom = [entity.strip() for entity in open(self.path_symptom) if entity.strip()]
        self.words_department = [entity.strip() for entity in open(self.path_department) if entity.strip()]
        self.words_check = [entity.strip() for entity in open(self.path_check) if entity.strip()]
        self.words_food = [entity.strip() for entity in open(self.path_food) if entity.strip()]
        self.words_drug = [entity.strip() for entity in open(self.path_drug) if entity.strip()]
        self.words_producer = [entity.strip() for entity in open(self.path_producer) if entity.strip()]

        self.words_all = list(set(self.words_disease + self.words_symptom + self.words_department + self.words_check 
            + self.words_food + self.words_drug + self.words_producer))
        # the mecical entity Aho-Corasick auto machine
        self.medical_tree = self.construct_actree(self.words_all)
        # the medical word type dictionary
        self.wordtype_dict = self.construct_wordtype_dict(self.words_all)

        # question words
        with open(self.path_question_words, 'r') as fjson:
            question_words = json.load(fjson)
        self.deny_words = question_words['deny_words']
        self.qwds_symptom = question_words['qwds_symptom']
        self.qwds_department = question_words['qwds_department']
        self.qwds_cause = question_words['qwds_cause']
        self.qwds_check = question_words['qwds_check']
        self.qwds_food = question_words['qwds_food']
        self.qwds_drug = question_words['qwds_drug']
        self.qwds_producer = question_words['qwds_producer']
        self.qwds_cureway = question_words['qwds_cureway']
        self.qwds_cureprob = question_words['qwds_cureprob']
        self.qwds_complication = question_words['qwds_complication']
        self.qwds_prevent = question_words['prevent']
        self.qwds_duration = question_words['qwds_duration']
        self.qwds_easyget = question_words['qwds_easyget']
        self.qwds_cure = question_words['qwds_cure']

    '''get the dictionary of question entities and their own types'''
    def get_question_entity_type(self, question):
        question_words = []
        for tp in self.medical_tree.iter(question):
            word = tp[1][1]
            question_words.append(word)
        short_sub_words = []
        for wd1 in question_words:
            for wd2 in question_words:
                if wd1 in wd2 and wd1 != wd2:
                    short_sub_words.append(wd1)
        final_words = [wd for wd in question_words if wd not in short_sub_words]
        question_entity_type_dict = {wd : self.wordtype_dict.get(wd) for wd in final_words}
        return question_entity_type_dict

    '''construct the Aho-Corasick auto machine to accelerate the entities matching process'''
    def construct_actree(self, wordlist):
        # construct the Trie tree
        actree = ahocorasick.Automaton()
        for idx, key in enumerate(wordlist):
            actree.add_word(key, (idx, key))
        # transfer the Trie tree into Aho-Corasick auto machine
        actree.make_automaton()
        return actree

    '''construct the word and type dictionary'''
    def construct_wordtype_dict(self, wordlist):
        wordtype_dict = {}
        for word in wordlist:
            wordtype_dict[word] = []
            if word in self.words_disease:
                wordtype_dict[word].append(mc.DISEASE)
            if word in self.words_symptom:
                wordtype_dict[word].append(mc.SYMPTOM)
            if word in self.words_department:
                wordtype_dict[word].append(mc.DEPARTMENT)
            if word in self.words_check:
                wordtype_dict[word].append(mc.CHECK)
            if word in self.words_food:
                wordtype_dict[word].append(mc.FOOD)
            if word in self.words_drug:
                wordtype_dict[word].append(mc.DRUG)
            if word in self.words_producer:
                wordtype_dict[word].append(mc.PRODUCER)
        return wordtype_dict

    '''check whether the words exist in question'''
    def check_words(self, wordlist, question):
        for word in wordlist:
            if word in question:
                return True
        return False

    '''the main function to parse the question'''
    def parse_question(self, question):
        question_meta = {}
        question_entity_type = self.get_question_entity_type(question)
        if question_entity_type == {}:
            return {}
        question_meta['args'] = question_entity_type
        # the types of entities that present in question
        types = []
        for _type in question_entity_type.values():
            types += _type

        # the type of question, based on the entities types of question and the question keywords
        question_types = []
        # symptom
        if self.check_words(self.qwds_symptom, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_SYMP)
        if self.check_words(self.qwds_symptom, question) and mc.SYMPTOM in types:
            question_types.append(mc.SYMP_DISE)
        
        # department
        if self.check_words(self.qwds_department, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_DEPART)
        
        # cause
        if self.check_words(self.qwds_cause, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_CAUSE)

        # check
        if self.check_words(self.qwds_check, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_CHECK)
        if self.check_words(self.qwds_check, question) and mc.CHECK in types:
            question_types.append(mc.CHECK_DISE)
        
        # drug
        if self.check_words(self.qwds_drug, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_DRUG)
        # food
        elif self.check_words(self.qwds_food, question) and mc.DISEASE in types:
            deny_status = self.check_words(self.deny_words, question)
            if deny_status:
                question_types.append(mc.DISE_NOT_FOOD)
            else:
                question_types.append(mc.DISE_DO_FOOD)

        # complication
        if self.check_words(self.qwds_complication, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_COMP)

        # cure probbility
        if self.check_words(self.qwds_cureprob, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_CUREPROB)

        # cure way
        if self.check_words(self.qwds_cureway, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_CUREWAY)

        # drug -> disease
        if self.check_words(self.qwds_cure, question) and mc.DRUG in types:
            question_types.append(mc.DRUG_DISE)

        # duration
        if self.check_words(self.qwds_duration, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_DURA)

        # prevent
        if self.check_words(self.qwds_prevent, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_PREV)

        # easy get
        if self.check_words(self.qwds_easyget, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_EASY)

        # if the question_types is empty
        if question_types == [] and mc.DISEASE in types:
            question_types.append(mc.DISE_DESC)
        elif question_types == [] and mc.SYMPTOM in types:
            question_types.append(mc.SYMP_DISE)
        elif question_types == [] and mc.DRUG in types:
            question_types.append(mc.DRUG_DISE)
        elif question_types == [] and mc.CHECK in types:
            question_types.append(mc.CHECK_DISE)

        question_meta['question_types'] = list(set(question_types))
        return question_meta


'''testing code'''
if __name__ == '__main__':
    parser = QuestionParser()
    while 1:
        question = input("question: ")
        meta = parser.main_parser(question)
        print(meta)
