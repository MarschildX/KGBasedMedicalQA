# coding=utf-8
import pymongo
from lxml import etree
import os

class MedicalGraph:
    def __init__(self):
        self.conn = pymongo.MongoClient()
        cur_dir = '/'.join(os.path.abspath(__file__).split('/')[:-1])
        self.db = self.conn['medical']
        self.col = self.db['raw_data']
        alphabets = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y', 'z']
        nums = ['1','2','3','4','5','6','7','8','9','0']
        self.stop_words =  alphabets + nums
        self.key_dict = {
            '医保疾病' : 'yibao_status',
            "患病比例" : "get_prob",
            "易感人群" : "easy_get",
            "传染方式" : "get_way",
            "就诊科室" : "cure_department",
            "治疗方式" : "cure_way",
            "治疗周期" : "cure_lasttime",
            "治愈率" : "cured_prob",
            '药品明细': 'drug_detail',
            '药品推荐': 'recommand_drug',
            '推荐': 'recommand_eat',
            '忌食': 'not_eat',
            '宜食': 'do_eat',
            '症状': 'symptom',
            '检查': 'check',
            '成因': 'cause',
            '预防措施': 'prevent',
            '所属类别': 'category',
            '简介': 'desc',
            '名称': 'name',
            '常用药品' : 'common_drug',
            '治疗费用': 'cost_money',
            '并发症': 'acompany'
        }

    def collect_medical(self):
        cates = []
        inspects = []
        count = 0
        for item in self.col.find():
            data = {}
            basic_info = item['basic_info']
            name = basic_info['name']
            if not name:
                continue

            # basic info processing
            data['名称'] = name
            data['简介'] = '\n'.join(basic_info['desc']).replace('\r\n\t', '').replace('\r\n\n\n','').replace(' ','').replace('\r\n','\n')
            category = basic_info['category']
            data['所属类别'] = category
            cates += category
            inspect = item['inspect_info']
            inspects += inspect
            attributes = basic_info['attributes']

            # causing and preventing
            data['预防措施'] = item['prevent_info']
            data['成因'] = item['cause_info']
            
            # symptom data
            data['症状'] = item['symptom_info']

            # a variety of attributes from basic infobox, the attributes data shown as follow:
            '''
            基本知识
            医保疾病：否
            患病比例：0.01%--0.02%
            易感人群：儿童
            传染方式：无传染性
            并发症：休克 菌血症 脑膜炎 败血症
            治疗常识
            就诊科室：儿科 小儿内科
            治疗方式：药物治疗 支持性治疗
            治疗周期：4-8周
            治愈率：85%--98%
            常用药品：羧甲司坦片 肺力咳合剂
            治疗费用：根据不同医院，收费标准不一致，市三甲医院约(3000 —— 6000元）
            '''
            for attr in attributes:
                attr_pair = attr.split('：')
                if len(attr_pair) == 2:
                    key = attr_pair[0]
                    value = attr_pair[1]
                    data[key] = value

            # inspect data, stored as 'check' or '检查'
            inspects = item['inspect_info']
            data['检查'] = inspects

            # food data
            food_info = item['food_info']
            if food_info:
                data['宜食'] = food_info['good']
                data['忌食'] = food_info['bad']
                data['推荐'] = food_info['recommand']

            # drug data
            drug_info = item['drug_info']
            data['药品推荐'] = list(set([i.split('(')[-1].replace(')','') for i in drug_info]))
            data['药品明细'] = drug_info # this item means the producer of a specific drug.

            # at last, modify the data
            data_modify = {}
            for attr, value in data.items():
                attr_en = self.key_dict.get(attr)
                if attr_en:
                    data_modify[attr_en] = value
                if attr_en in ['yibao_status', 'get_prob', 'easy_get', 'get_way', "cure_lasttime", "cured_prob"]: # 
                    data_modify[attr_en] = value.replace(' ','').replace('\t','')
                elif attr_en in ['cure_department', 'cure_way', 'common_drug']: # department, cure_way and common_drug come from basic infobox
                    data_modify[attr_en] = [i for i in value.split(' ') if i]
                elif attr_en in ['acompany']: # 并发症
                    acompany = [i for i in value.split(' ') if i]
                    data_modify[attr_en] = acompany
            try:
                self.db['tidy_data'].insert(data_modify)
                count += 1
                print(count)
            except Exception as e:
                print(e)
        return


if __name__ == '__main__':
    handler = MedicalGraph()
    handler.collect_medical()
