# coding=utf-8
import urllib.request
import urllib.parse
from lxml import etree
import re
import pymongo

class CrimeSpider:
    def __init__(self):
        self.conn = pymongo.MongoClient()
        self.db = self.conn['medical']
        self.col = self.db['raw_data']
        return

    '''request html page according to url'''
    def get_html(self, url):
        headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) '
                                 'Chrome/51.0.2704.63 Safari/537.36'}
        req = urllib.request.Request(url=url, headers=headers)
        res = urllib.request.urlopen(req)
        html = res.read().decode('gbk')
        return html

    '''parsing url'''
    def url_parser(self, content):
        selector = etree.HTML(content)
        urls = ['http://www.anliguan.com' + i for i in  selector.xpath('//h2[@class="item-title"]/a/@href')]
        return urls

    '''main crawler process'''
    def spider_main(self):
        for page in range(1, 11000):
            try:
                basic_url = 'http://jib.xywy.com/il_sii/gaishu/%s.htm'%page
                cause_url = 'http://jib.xywy.com/il_sii/cause/%s.htm'%page
                prevent_url = 'http://jib.xywy.com/il_sii/prevent/%s.htm'%page
                symptom_url = 'http://jib.xywy.com/il_sii/symptom/%s.htm'%page 
                inspect_url = 'http://jib.xywy.com/il_sii/inspect/%s.htm'%page 
                treat_url = 'http://jib.xywy.com/il_sii/treat/%s.htm'%page
                food_url = 'http://jib.xywy.com/il_sii/food/%s.htm'%page
                drug_url = 'http://jib.xywy.com/il_sii/drug/%s.htm'%page
                data = {}
                data['url'] = basic_url
                data['basic_info'] = self.basicinfo_spider(basic_url)
                data['cause_info'] =  self.common_spider(cause_url)
                data['prevent_info'] =  self.common_spider(prevent_url)
                data['symptom_info'] = self.symptom_spider(symptom_url) # symptom data is long message instead of short medical entities.
                data['inspect_info'] = self.inspect_spider(inspect_url) # inspect data is also long message, composed with a lot of detail description.
                data['treat_info'] = self.treat_spider(treat_url)
                data['food_info'] = self.food_spider(food_url)
                data['drug_info'] = self.drug_spider(drug_url)
                self.col.insert_one(data)
                print(page, basic_url)
            except Exception as e:
                print(e, page)
        return

    '''crawling and parsing the basic information'''
    def basicinfo_spider(self, url):
        html = self.get_html(url)
        selector = etree.HTML(html)
        title = selector.xpath('//title/text()')[0]
        category = selector.xpath('//div[@class="wrap mt10 nav-bar"]/a/text()')
        desc = selector.xpath('//div[@class="jib-articl-con jib-lh-articl"]/p/text()')
        ps = selector.xpath('//div[@class="mt20 articl-know"]/p')
        # the infobox data shown as follow:
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
        infobox = []
        for p in ps:
            info = p.xpath('string(.)').replace('\r','').replace('\n','').replace('\xa0', '').replace('   ', '').replace('\t','')
            infobox.append(info)
        basic_data = {}
        basic_data['category'] = category
        basic_data['name'] = title.split('的简介')[0]
        basic_data['desc'] = desc
        basic_data['attributes'] = infobox
        print(basic_data['attributes'])
        return basic_data

    '''crawling and parsing food data'''
    def food_spider(self, url):
        html = self.get_html(url)
        selector = etree.HTML(html)
        divs = selector.xpath('//div[@class="diet-img clearfix mt20"]')
        print('divs number: ', len(divs))
        try:
            food_data = {}
            food_data['good'] = divs[0].xpath('./div/p/text()') # return a good food list, for instance ['南瓜子仁', '圆白菜', '樱桃番茄', '小白菜']
            food_data['bad'] = divs[1].xpath('./div/p/text()')
            food_data['recommand'] = divs[2].xpath('./div/p/text()')
        except:
            return {}
        print(food_data)
        return food_data

    '''crawling and parsing the drug data'''
    def drug_spider(self, url):
        html = self.get_html(url)
        selector = etree.HTML(html)
        drugs = [i.replace('\n','').replace('\t', '').replace(' ','') for i in selector.xpath('//div[@class="fl drug-pic-rec mr30"]/p/a/text()')]
        print(drugs)
        return drugs

    '''the symptom data consists of long message.'''
    def symptom_spider(self, url):
        html = self.get_html(url)
        selector = etree.HTML(html)
        ps = selector.xpath('//div[@class="jib-articl fr f14 jib-lh-articl"]//p')
        detail = []
        for p in ps:
            info = p.xpath('string(.)').replace('\r','').replace('\n','').replace('\xa0', '').replace('   ', '').replace('\t','')
            detail.append(info)
        print(detail) # the symptom message recorded here
        return detail

    '''crawling and parsing the inspect data, which consists of long message instead of short entities.'''
    def inspect_spider(self, url):
        html = self.get_html(url)
        selector = etree.HTML(html)
        inspects  = selector.xpath('//div[@class="jib-articl fr f14 jib-lh-articl"]/p')
        inspects_str = []
        for p in inspects:
            tmp = p.xpath('string(.)').replace('\t', '').replace('\r', '').replace('\n', '')
            if tmp:
                inspects_str.append(tmp)
        print(inspects_str)
        return inspects_str

    '''common crawler is used to crawl causing and preventing data.'''
    def common_spider(self, url):
        html = self.get_html(url)
        selector = etree.HTML(html)
        ps = selector.xpath('//p')
        infobox = []
        for p in ps:
            info = p.xpath('string(.)').replace('\r', '').replace('\n', '').replace('\xa0', '').replace('   ','').replace('\t', '')
            if info:
                infobox.append(info)
        return '\n'.join(infobox)

    '''crawling and parsing treating data.'''
    def treat_spider(self, url):
        html = self.get_html(url)
        selector = etree.HTML(html)
        ps = selector.xpath('//div[starts-with(@class,"mt20 articl-know")]/p')
        infobox = []
        for p in ps:
            info = p.xpath('string(.)').replace('\r','').replace('\n','').replace('\xa0', '').replace('   ', '').replace('\t','')
            infobox.append(info)
        return infobox


handler = CrimeSpider()
handler.spider_main()