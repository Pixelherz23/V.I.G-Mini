from re import I
import flask
from flask import jsonify, render_template, request, redirect, url_for, session
import simplejson as json
from decimal import Decimal
from datetime import datetime
import pytz
import mysql.connector as mysql
from passlib.hash import sha256_crypt
from mysql.connector import errorcode as errcode

# Encodes Decimal to String for Json
class MyJSONEncoder(flask.json.JSONEncoder):

    def default(self, obj):
        if isinstance(obj, Decimal):
            # Convert decimal instances to strings.
            return str(obj)
        return super(MyJSONEncoder, self).default(obj)

databaseConf = {
    'user': 'vigmini',
    'password': 'Entry4NoOne299!29/',
    'host': '127.0.0.1',
    'database': 'VIGMini',
    'raise_on_warnings': True
}

app = flask.Flask(__name__)
app.config["DEBUG"] = True
app.json_encoder = MyJSONEncoder

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

def check_password(email, pw):
    pw_db = executeQuery('SELECT PASSWORD FROM TBL_USER WHERE EMAIL = ', email)
    return sha256_crypt.verify(pw, pw_db)

# returns the system time
@app.route('/time', methods=['GET'])
def time():
    now = datetime.now(pytz.timezone("Europe/Berlin"))
    current_time = now.strftime("%H:%M:%S")
    return current_time

# returns the news informations from the DB
@app.route('/information/news', methods=['GET'])
def news():
    all_news = executeQuery('SELECT * FROM NEWS')
    return jsonify(all_news)

# returns the help informations from the DB
@app.route('/information/help', methods=['GET'])
def help():
    all_help = executeQuery('SELECT * FROM HELP')
    return jsonify(all_help)

#  returns the newest measurments
@app.route('/greenhouse/measurements/now', methods=['GET'])
def measurements_now():
    product_key = request.args.get('product_key')
    day_measurements = executeQuery('SELECT * FROM MEASUREMENTS_NOW WHERE PRODUCT_KEY = ', product_key)
    return jsonify(day_measurements)

#  returns the measurments from the last day
@app.route('/greenhouse/measurements/day', methods=['GET'])
def measurements_day():
    product_key = request.args.get('product_key')
    day_measurements = executeQuery('SELECT * FROM MEASUREMENTS_DAY WHERE PRODUCT_KEY = ', product_key)
    return jsonify(day_measurements)

#  returns the measurments from the last week
@app.route('/greenhouse/measurements/week', methods=['GET'])
def measurements_week():
    product_key = request.args.get('product_key')
    week_measurements = executeQuery('SELECT * FROM MEASUREMENTS_WEEK WHERE PRODUCT_KEY = ', product_key)
    return jsonify(week_measurements)

#  returns the measurments from the last month
@app.route('/greenhouse/measurements/month', methods=['GET'])
def measurements_month():
    product_key = request.args.get('product_key')
    month_measurements = executeQuery('SELECT * FROM MEASUREMENTS_MONTH WHERE PRODUCT_KEY = ', product_key)
    return jsonify(month_measurements)

# inserts new measuremnts to the DB
# neue URL /greenhouse/measurements/new
@app.route('/greenhouse/measurements/new', methods=["POST"])
def new_measurements():
    data = request.get_json()
    if 'product_key' in data and 'led_status' in data and 'temperature' in data and 'humidity' in data and 'soil_moisture' in data:
        conn = connDB()
        cur = conn.cursor()
        print(data['product_key'], data['led_status'], float(data['temperature']), int(data['humidity']), float(data['soil_moisture']),  sep="\n")
        proc = cur.callproc('NEW_MEASUREMENTS', 
            (data['product_key'], data['led_status'], float(data['temperature']), int(data['humidity']), float(data['soil_moisture']), None))
        if proc[5] == 'Success':
            conn.commit()
        cur.close()
        conn.close()
    return('ERROR')

# activates the Greenhouse and connects user with it
@app.route('/greenhouse/activate', methods=['GET'])
def activate_greenhouse():
    product_key = request.args.get('product_key')
    email = request.args.get('e-mail')
    name = request.args.get('name')

    if product_key and email:
        conn = connDB()
        cur = conn.cursor()
        if name == None:
            name = 'Gewächshaus'
        proc = cur.callproc('ACTIVATE_GREENHOUSE', (product_key, email, name, None))
        if proc[3] == 'Success':
            conn.commit()
        cur.close()
        conn.close()
        return proc[3]
    return 'Es Fehlen angaben!!'


# change the name of the greenhouse
@app.route('/greenhouse/update/name', methods=['POST'])
def change_greenhouse_name():
    product_key = request.form.get('product_key')
    name = request.form.get('name')

    if product_key and name:
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('CHANGE_GREENHOUSE_NAME', (product_key, name, None))
        if proc[2] == 'Success':
            conn.commit()
        cur.close()
        conn.close()
        return proc[2]

# updates the settings
@app.route('/greenhouse/update/settings', methods=['POST'])
def update_settings():
    product_key = request.form.get('product_key')
    max_temp = request.form.get('max_temp')
    min_soil_moisture = request.form.get('min_soil_moisture')
    temp_on = request.form.get('temp_on')
    soil_moisture_on = request.form.get('soil_moisture_on')
    timetable_type = request.form.get('timetable_type')
    interval = request.form.get('interval')
    from_time = request.form.get('from_time')
    to_time = request.form.get('to_time')
    timetable_on = request.form.get('timetable_on')

    
    if product_key and (max_temp or min_soil_moisture or temp_on or soil_moisture_on or (timetable_type and interval and (from_time or to_time or timetable_on))):
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('UPDATE_SETTINGS', (product_key, max_temp, min_soil_moisture, temp_on, soil_moisture_on, timetable_type, interval, from_time, to_time, timetable_on, None))
        if proc[10] == 'Success':
            conn.commit()
        cur.close()
        conn.close()
        return proc[10]
    else:
        return('Bitte Werte zum ändern eingeben')


@app.route('/greenhouse/settings/temperature', methods=['GET'])
def temp_settings():
    product_key = request.args.get('product_key')
    if product_key:
        if executeQuery('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = ', product_key, ')'):
            data = executeQuery('SELECT MAX_TEMPERATURE, TEMP_ON FROM SETTINGS WHERE PRODUCT_KEY = ', product_key)
            return jsonify(data)
        return('Gewächshaus existiert  nicht!')
    return('Kein Produktschlüssel übergeben')


@app.route('/greenhouse/settings/soil-moisture/value', methods=['GET'])
def soil_moisture_settings_value():
    product_key = request.args.get('product_key')
    if product_key:
        if executeQuery('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = ', product_key, ')'):
            data = executeQuery('SELECT SOIL_MOISTURE_ON, MIN_SOIL_MOISTURE FROM SETTINGS WHERE PRODUCT_KEY = ', product_key)
            return jsonify(data)
        return('Gewächshaus existiert nicht!')
    return('Kein Produktschlüssel übergeben')


@app.route('/greenhouse/settings/soil-moisture/time', methods=['GET'])
def soil_moisture_settings_time1():
    product_key = request.args.get('product_key')
    interval = request.args.get('interval')
    if product_key and interval:
        if executeQuery('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = ', product_key, ')'):
            query_select = 'SELECT SETTINGS.SOIL_MOISTURE_ON, TIMETABLE.TIMETABLE_ON, TIMETABLE.FROM_TIME FROM SETTINGS '
            query_join = 'FULL OUTER JOIN TIMETABLE ON SETTINGS.ID_SETTINGS = TIMETABLE.ID_SETTINGS '
            query_where = 'WHERE SETTINGS.PRODUCT_KEY = ', product_key, ' AND TIMETABLE.INTERVAL_TIME = ', interval, ' AND TIMETABLE.TIMETABLE_TYPE = "soilMoisture"'
            data = executeQuery(query_select, query_join, query_where)
            return jsonify(data)
        return('Gewächshaus existiert nicht!')
    return('Kein Produktschlüssel übergeben')


@app.route('/greenhouse/settings/light', methods=['GET'])
def light_settings():
    product_key = request.args.get('product_key')
    interval = request.args.get('interval')
    if product_key and interval:
        if executeQuery('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = ', product_key, ')'):
            query_select = 'SELECT TIMETABLE.TIMETABLE_ON, TIMETABLE.FROM_TIME, TIMETABLE.TO_TIME FROM SETTINGS '
            query_join = 'FULL OUTER JOIN TIMETABLE ON SETTINGS.ID_SETTINGS = TIMETABLE.ID_SETTINGS '
            query_where = 'WHERE SETTINGS.PRODUCT_KEY = ', product_key, ' AND TIMETABLE.INTERVAL_TIME = ', interval, ' AND TIMETABLE.TIMETABLE_TYPE = "light"'
            data = executeQuery(query_select, query_join, query_where)
            return jsonify(data)
        return('Gewächshaus existiert nicht!')
    return('Kein Produktschlüssel übergeben')

# Creates a new user into the DB when recieve a get or post
# neue URL /user/new
@app.route('/user/new', methods=['GET', 'POST'])
def new_user():
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
        return proc[4]
            
    else:
        return('Bitte alle felder ausfüllen')


@app.route('/user/update/information', methods=['POST'])
def update_user():
    firstname = request.form.get('firstname')
    lastname = request.form.get('lastname')
    email = request.form.get('e-mail')
    new_email = request.form.get('new_e-mail')
    password = request.form.get('password')
    new_password = request.form.get('new_password')

    if email and firstname and lastname:
        if password and new_password:
            if check_password(email, password):
                new_password = sha256_crypt.encrypt(new_password)
            else:
                return('Passwort falsch!')
        if new_email == email:
            new_email = None
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('UPDATE_USER', (firstname, lastname, email, new_email, new_password, None))
        if proc[5] == 'Success':
            conn.commit()
        cur.close()
        conn.close()
        return proc[5]

    else:
        return('Bitte alle felder ausfüllen')


@app.route('/user/login', methods=['POST'])
def user_login():
    email = request.form.get('e-mail')
    password = request.form.get('password')
    if email and password:
        if executeQuery('SELECT EXISTS (SELECT 1 FROM TBL_USER WHERE EMAIL = ', email, ')'):
            if check_password(email, password):
                return('Success')
        return('E-Mail oder Passwort falsch!')
    return('Bitte alle Felder ausfüllen!')


@app.route('/user/information/first-name', methods=['GET'])
def user_first_name():
    email = request.args.get('e-mail')
    if email:
        if executeQuery('SELECT EXISTS (SELECT 1 FROM TBL_USER WHERE EMAIL = ', email, ')'):
            return executeQuery('SELECT FIRS_TNAME FROM TBL_USER WHERE EMAIL = ', email)
        return ('User existiert nicht!')
    return('Email nicht übergeben!')


@app.route('/user/information/last-name', methods=['GET'])
def user_last_name():
    email = request.args.get('e-mail')
    if email:
        if executeQuery('SELECT EXISTS (SELECT 1 FROM TBL_USER WHERE EMAIL = ', email, ')'):
            return executeQuery('SELECT LAST_NAME FROM TBL_USER WHERE EMAIL = ', email)
        return ('User existiert nicht!')
    return('Email nicht übergeben!')
    


def main():   
    app.run(host="0.0.0.0")
    


main()