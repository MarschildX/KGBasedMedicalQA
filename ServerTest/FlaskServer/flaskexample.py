from flask import Flask, make_response
 
app = Flask(__name__)
uName = "admin"
uPwd = "admin"
status = ""
 
 
# 登陆函数( REST RESTful：接受参数，访问类型可以为：get、post)
@app.route("/login/<account>/<password>", methods=['GET', 'POST'])
def get_content(account, password):
    if uName.__eq__(account) and uPwd.__eq__(password):
        status = "200"
        rst = make_response(status)
        #处理请求头部
        rst.headers['Access-Control-Allow-Origin'] = '*'
        return rst
 
    status = "0"
    rst = make_response(status)
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst
 
 
if __name__ == '__main__':
    app.run(host="0.0.0.0", port="80", debug=True)
