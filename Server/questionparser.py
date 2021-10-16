import ahocorasick
import os
import medicalconst as mc


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

        # question key words
        self.deny_words = ['否', '非', '不', '无', '弗', '勿', '毋', '未', '没', '莫', '没有', '防止', '不再', '不会', 
            '不能', '忌', '禁止', '防止', '难以', '忘记', '忽视', '放弃', '拒绝', '杜绝', '不是', '并未', '并无', '仍未', 
            '难以出现', '切勿', '不要', '不可', '别', '管住', '注意', '小心', '少']
        self.qwds_symptom = ['表征', '症状', '表现','现象', '症候', '特点']
        self.qwds_department = ['科室', '属于', '哪个科', '属什么科', '属于什么科', '哪个科负责', '什么科负责', '哪个科室负责', '什么科室负责']
        self.qwds_cause = ['病因', '由什么引起', '原因', '怎么得的', '为什么会得', '如何引起', '为何', '为啥', '为什么', '成因', '为何得', 
            '怎么得', '导致', '造成', '怎么会']
        self.qwds_check = ['检查', '怎么查', '查什么', '查出', '测出']
        self.qwds_food = ['吃什么', '食物', '要吃', '不能吃', '忌口', '菜', '膳食', '不要吃', '吃的', '食疗', '保健品', '饮食', '食品', '吃', 
            '喝', '食', '补', '营养', '补充', '菜谱', '饮用']
        self.qwds_drug = ['药', '药品', '药片', '处方', '药方', '用药', '胶囊', '口服液', '吃什么药']
        self.qwds_producer = ['生产商', '厂商', '牌子', '厂', '品牌', '牌', '出品']
        self.qwds_cureway = ['怎么治疗', '如何治疗', '怎么治', '怎么医', '怎么医治', '如何医治', '怎么治愈', '如何治愈', '如何治','如何医', 
            '怎么办', '咋办', '咋治', '疗法']
        self.qwds_curepro = ['概率有多大', '几率有多大', '多大概率', '多大几率', '治愈率', '概率有多少', '多少概率', '多少几率', '几率有多少', 
            '概率', '几率', '希望有多少', '有多大希望', '希望有多大', '希望', '几成', '比例', '可能性']
        self.qwds_complication = ['并发症', '一起发生', '并发', '一并发生', '共同症状', '共同发生', '伴随', '一同出现', '共现']
        self.qwds_prevent = ['预防', '怎么预防', '如何预防', '防止', '如何防止', '怎么防止', '远离', '怎么远离', '如何远离', '避免', '怎么避免', 
            '如何避免', '怎样才不', '怎样才能不', '如何才能不', '如何才不', '防范']
        self.qwds_duration = ['几天痊愈', '持续多久', '几天才能痊愈', '几天才能好', '多久能好', '多久才能好', '多久痊愈', '多久能痊愈', 
            '多久才能痊愈', '多久', '周期', '持续多长时间', '几年', '几天', '几个小时']
        self.qwds_easyget = ['什么人容易得', '易得', '易患', '易感人群', '什么人', '哪些人', '容易感染', '容易染上', '易感染', '易染上', '容易得']
        self.qwds_cure = ['治什么', '治疗什么', '治哪些', '治疗哪些', '治疗啥', '医治啥', '治啥', '有什么用', '有啥用', '用处', '用途']

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
        
        # food
        if self.check_words(self.qwds_food, question) and mc.DISEASE in types:
            deny_status = self.check_words(self.deny_words, question)
            if deny_status:
                question_types.append(mc.DISE_NOT_FOOD)
            else:
                question_types.append(mc.DISE_DO_FOOD)

        # drug
        if self.check_words(self.qwds_drug, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_DRUG)

        # complication
        if self.check_words(self.qwds_complication, question) and mc.DISEASE in types:
            question_types.append(mc.DISE_COMP)

        # cure probbility
        if self.check_words(self.qwds_curepro, question) and mc.DISEASE in types:
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
        if question_types is [] and mc.DISEASE in types:
            question_types.append(mc.DISE_DESC)
        elif question_types is [] and mc.SYMPTOM in types:
            question_types.append(mc.SYMP_DISE)
        elif question_types is [] and mc.DRUG in types:
            question_types.append(mc.DRUG_DISE)


        question_meta['question_types'] = list(set(question_types))
        return question_meta


'''testing code'''
if __name__ == '__main__':
    parser = QuestionParser()
    while 1:
        question = input("question: ")
        meta = parser.main_parser(question)
        print(meta)
