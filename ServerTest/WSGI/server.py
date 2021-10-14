# coding:utf-8

from wsgiref.simple_server import make_server
from sayhello import hello

def main():
    server = make_server('localhost', 8001, hello)
    print('Serving HTTP on port 8001...')
    server.serve_forever()


if __name__ == '__main__':
    main()