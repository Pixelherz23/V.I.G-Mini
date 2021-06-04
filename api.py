import flask
from flask import jsonify, render_template, request, redirect, url_for, session
from datetime import datetime
import pytz
import mysql.connector as mysql
from passlib.hash import sha256_crypt
from mysql.connector import errorcode as errcode

databaseConf = {
    'user': 'vigmini',
    'password': 'Entry4NoOne299!29/',
    'host': '127.0.0.1',
    'database': 'VIGMini',
    'raise_on_warnings': True
}

app = flask.Flask(__name__)
app.config["DEBUG"] = True

#connect to the database
def connDB():
    try:
        conn = mysql.connect(**databaseConf)
        if(conn.is_connected):
            print("Connected to VIGMINI Database!")
    except mysql.Error as err:
        if err.errno == errcode.ER_ACCESS_DENIED_ERROR:
            print("Wrong password or username")
        elif err.errno == errcode.ER_BAD_DB_ERROR:
            print("Database does not exist")
        else:
            print(err)
    
    return conn

# executes the given query
def executeQuery(query):
    conn = connDB()
    cursor = conn.cursor()
    cursor.execute(query)
    data = cursor.fetchall()
    cursor.close()
    return data

@app.route('/', methods=['GET'])
def home():
    return "Hello World"

# returns the system time
@app.route('/time', methods=['GET'])
def time():
    now = datetime.now(pytz.timezone("Europe/Berlin"))
    current_time = now.strftime("%H:%M:%S")
    return current_time

# returns the news informations from the DB
@app.route('/information/news', methods=['Get'])
def news():
    all_news = executeQuery('SELECT * FROM NEWS')
    return jsonify(all_news)

# returns the help informations from the DB
@app.route('/information/help', methods=['Get'])
def help():
    all_help = executeQuery('SELECT * FROM HELP')
    return jsonify(all_help)

@app.route('/activate-Greenhouse', methods=['Get'])
def activate_greenhouse():
    return

# Creates a new user into the DB when recieve a get or post
@app.route('/newuser', methods=['Get', 'Post'])
def newuser():
    if request.method == 'POST':
        firstname = request.form.get('firstname')
        lastname = request.form.get('lastname')
        email = request.form.get('e-mail')
        password = request.form.get('password')
    else:
        firstname = request.args.get('firstname')
        lastname = request.args.get('lastname')
        email = request.args.get('e-mail')
        password = request.args.get('password')

    if firstname and lastname and email and password:
        conn = connDB()
        cur = conn.cursor()
        hash_pw = sha256_crypt.encrypt(password)
        proc = cur.callproc('NEW_USER', (firstname, lastname, email, hash_pw, None))
        if proc[4] == 'Success':
            conn.commit()
            cur.close()
            conn.close()
            return jsonify(proc)
        cur.close()
        conn.close()
        return("ERROR")
        
    else:
        return("Bitte alle felder ausfüllen")

@app.route('/newMeasuremnets', methods="POST")
def new_measurements():
    data = request.get_json()
    if 'product_key' in data and 'led_status' in data and 'temperature' in data and 'humidity' in data and 'soil_moisture' in data:
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('NEW_MEASUREMENTS', 
            (data['product_key'], data['led_status'], data['temperature'], data['humidity'], data['soil_moisture'], None))
        if proc[5] == 'Success':
            conn.commit()
            cur.close()
            conn.close()
            return jsonify(proc)
        cur.close()
        conn.close()
    return('ERROR')
    
    


def main():   
    app.run(host="0.0.0.0")
    


main()