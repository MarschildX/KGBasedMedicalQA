# coding:utf-8

def hello(environ, start_response):
    status = "200 OK"
    response_headers = [('Content-Type', 'text/html')]
    start_response(status, response_headers)
    path = environ['PATH_INFO'][1:] or 'hello'
    return [b'<h1> %s </h1>' % path.encode()]