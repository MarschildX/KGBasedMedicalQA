import os
import time

def feedback():
    prefix_path = '/'.join(os.path.abspath(__file__).split('/')[:-1])
    feedback_file_path = os.path.join(prefix_path, 'feedback')
    if not os.path.exists(feedback_file_path):
        os.makedirs(feedback_file_path)

    question = '心脏病该怎么治'
    feedback = '待完善'
    
    if question == '' or feedback == '':
        return
    ip = '106.15.90.100'
    mac = '10:10:10:10:20'
    user_id = ip + '$' + mac
    context_dict = {}
    context = str(context_dict)
    curr_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
    fb =curr_time + '---' + user_id + '---' + feedback + '---' + question + '---' + context + '\n'

    with open(feedback_file_path+'/user_feedback.txt', 'a') as ff:
        ff.write(fb)


if __name__ == '__main__':
    feedback()