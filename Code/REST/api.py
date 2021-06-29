
from flask import Flask, jsonify, json, request, Response
from decimal import Decimal
from datetime import datetime, timedelta
import pytz
import mysql.connector as mysql
from mysql.connector import errorcode as errcode
from passlib.hash import sha256_crypt
from flask_cors import CORS, cross_origin


# Encodes Decimal to String for Json
class MyJSONEncoder(json.JSONEncoder):

    def default(self, obj):
        if isinstance(obj, Decimal):
            # Convert decimal instances to strings.
            return str(obj)
        if isinstance(obj, timedelta):
            return str(obj)
        return super(MyJSONEncoder, self).default(obj)

databaseConf = {
    'user': 'vigmini',
    'password': 'Entry4NoOne299!29/',
    'host': '127.0.0.1',
    'database': 'VIGMini',
    'raise_on_warnings': True
}

app = Flask(__name__)
app.config["DEBUG"] = True
app.json_encoder = MyJSONEncoder
CORS(app)

@app.after_request # blueprint can also be app~~
def after_request(response):
    header = response.headers
    header['Access-Control-Allow-Origin'] = '*'
    return response
    
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
            print("ERROR: %s", err)
    
    return conn

# executes the given query
def executeQuery(query, dict=False):
    conn = connDB()
    cursor = conn.cursor(dictionary=dict)
    cursor.execute(query)
    data = cursor.fetchall()
    cursor.close()
    return data

def executeQueryData(query, data, dict=False):
    conn = connDB()
    cursor = conn.cursor(dictionary=dict)
    cursor.execute(query, data)
    return_data = cursor.fetchall()
    cursor.close()
    return return_data


def check_password(email, pw):
    pw_db = executeQueryData('SELECT PASSWORD FROM TBL_USER WHERE EMAIL = %s', (email,))
    return sha256_crypt.verify(pw, pw_db[0][0])

# returns the system time
@app.route('/time', methods=['GET'])
def time():
    now = datetime.now(pytz.timezone("Europe/Berlin"))
    current_time = now.strftime("%H:%M:%S")
    return current_time

# returns the news informations from the DB
# is working
@app.route('/information/news', methods=['GET'])
def news():
    all_news = executeQuery('SELECT * FROM NEWS', True)
    if all_news:
        return jsonify(all_news)
    else:
        # HTTP status code 400 Bad Request
        return Response(status=400)
# returns the help informations from the DB
# is working
@app.route('/information/help', methods=['GET'])
def help():
    all_help = executeQuery('SELECT * FROM HELP', True)
    if all_help:
        return jsonify(all_help)
    else:
        # HTTP status code 400 Bad Request
        return Response(status=400)

@app.route('/greenhouse/all', methods=['GET'])
def greenhouse():
    email = request.args.get('e-mail')
    if email:
        greenhouses = executeQueryData('SELECT PRODUCT_KEY, NAME FROM GREENHOUSE WHERE EMAIL = %s', (email,), True)
        return jsonify(greenhouses)
    else:
        # HTTP status  code 400 Bad Request (please enter all fields)
        return Response(status=400)
    

#  returns the newest measurments
# is working
@app.route('/greenhouse/measurements/now', methods=['GET'])
def measurements_now():
    product_key = request.args.get('product_key')
    if product_key:
        day_measurements = executeQueryData('SELECT TEMPERATURE, HUMIDITY, SOIL_MOISTURE, TIME_STAMP FROM MEASUREMENTS_NOW WHERE PRODUCT_KEY = %s', (product_key,), True)[0]
        return jsonify(day_measurements)
    else:
        # HTTP status  code 400 Bad Request (please enter all fields)
        return Response(status=400)

#  returns the measurments from the last day
# is working
@app.route('/greenhouse/measurements/day', methods=['GET'])
def measurements_day():
    product_key = request.args.get('product_key')
    if product_key:
        day_measurements = executeQueryData('SELECT TEMPERATURE, HUMIDITY, SOIL_MOISTURE, TIME_STAMP FROM MEASUREMENTS_DAY WHERE PRODUCT_KEY = %s', (product_key,), True)
        return jsonify(day_measurements)
    else:
        # HTTP status  code 400 Bad Request (please enter all fields)
        return Response(status=400)

#  returns the measurments from the last week
# is working
@app.route('/greenhouse/measurements/week', methods=['GET'])
def measurements_week():
    product_key = request.args.get('product_key')
    if product_key:
        week_measurements = executeQueryData('SELECT TEMPERATURE, HUMIDITY, SOIL_MOISTURE, TIME_STAMP FROM MEASUREMENTS_WEEK WHERE PRODUCT_KEY = %s', (product_key,), True)
        return jsonify(week_measurements)
    else:
        # HTTP status  code 400 Bad Request (please enter all fields)
        return Response(status=400)

#  returns the measurments from the last month
# is working
@app.route('/greenhouse/measurements/month', methods=['GET'])
def measurements_month():
    product_key = request.args.get('product_key')
    if product_key:
        month_measurements = executeQueryData('SELECT TEMPERATURE, HUMIDITY, SOIL_MOISTURE, TIME_STAMP FROM MEASUREMENTS_MONTH WHERE PRODUCT_KEY = %s', (product_key,), True)
        return jsonify(month_measurements)
    else:
        # HTTP status  code 400 Bad Request (please enter all fields)
        return Response(status=400)

# inserts new measuremnts to the DB
# is working
@app.route('/greenhouse/measurements/new', methods=["POST"])
def new_measurements():
    data = request.get_json()
    if 'product_key' in data and 'led_status' in data and 'temperature' in data and 'humidity' in data and 'soil_moisture' in data:
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('NEW_MEASUREMENTS', 
            (data['product_key'], data['led_status'], float(data['temperature']), int(data['humidity']), float(data['soil_moisture']), None))
        if proc[5] == 'Success':
            conn.commit()
            # HTTP status code 201 Created
            response = Response(status=201)
        else:
            # HTTP status code 404 Not Found (Greenhouse does not exist)
            response = Response(status=404)
        cur.close()
        conn.close()
        return response
    # HTTP status  code 400 Bad Request (please enter all fields)
    return Response(status=400)

# is working
# activates the Greenhouse and connects user with it
@app.route('/greenhouse/activate', methods=['GET'])
def activate_greenhouse():
    product_key = request.args.get('product_key')
    email = request.args.get('e-mail')

    if product_key and email:
        response = None
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('ACTIVATE_GREENHOUSE', (product_key, email, None))
        if proc[2] == 'Success':
            conn.commit()
            # HTTP status code 201 Created
            response = Response(status=201)
        elif proc[2] == 'NoEmail':
            # HTTP status code 404 Not Found (email does not exist)
            response = Response(status=404)
        else:
            # HTTP status code 404 Not Found (product key does not exist)
            response = Response(status=404)
        cur.close()
        conn.close()
        return response
    # HTTP status  code 400 Bad Request (please enter all fields)
    return Response(status=400)

# is working
# change the name of the greenhouse
@app.route('/greenhouse/update/name', methods=['GET'])
def change_greenhouse_name():
    if request.method == 'POST':
        product_key = request.form.get('product_key')
        name = request.form.get('name')
    else:
        product_key = request.args.get('product_key')
        name = request.args.get('name')

    if product_key and name:
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('CHANGE_GREENHOUSE_NAME', (product_key, name, None))
        if proc[2] == 'Success':
            conn.commit()
            # HTTP status code 200 OK
            response = Response(status=200)
        else:
            # HTTP status code 404 Not Found (Greenhouse does not exist)
            response = Response(status=404)
        cur.close()
        conn.close()
        return response
    # HTTP status code 400 Bad Request (please enter all fields)
    return Response(status=400)

# updates the settings
# is working
@app.route('/greenhouse/settings/update', methods=['GET', 'POST'])
def update_settings():
    if request.method == 'POST':
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
    else:
        product_key = request.args.get('product_key')
        max_temp = request.args.get('max_temp')
        min_soil_moisture = request.args.get('min_soil_moisture')
        temp_on = request.args.get('temp_on')
        soil_moisture_on = request.args.get('soil_moisture_on')
        timetable_type = request.args.get('timetable_type')
        interval = request.args.get('interval')
        from_time = request.args.get('from_time')
        to_time = request.args.get('to_time')
        timetable_on = request.args.get('timetable_on')

    if product_key and (max_temp or min_soil_moisture or temp_on or soil_moisture_on or (timetable_type and interval and (from_time or to_time or timetable_on))):
        response = None
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('UPDATE_SETTINGS', (product_key, max_temp, min_soil_moisture, temp_on, soil_moisture_on, timetable_type, interval, from_time, to_time, timetable_on, None))
        if proc[10] == 'Success':
            conn.commit()
            # HTTP status code 200 OK
            response = Response(status=200)
        else:
            # HTTP status code 404 Not Found (Greenhouse does not exist)
            response = Response(status=404)
        cur.close()
        conn.close()
        return response
    # HTTP status code 400 Bad Request (please enter all fields)
    return Response(status=400)

#  is working
@app.route('/greenhouse/settings/temperature', methods=['GET'])
def temp_settings():
    product_key = request.args.get('product_key')
    if product_key:
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = %s)', (product_key,))[0][0]:
            data = executeQueryData('SELECT MAX_TEMPERATURE, TEMP_ON FROM SETTINGS WHERE PRODUCT_KEY = %s ', (product_key,), True)[0]
            return jsonify(data)
        # HTTP status code 404 Not Found (Greenhouse does not exist)
        return Response(status=404)
    # HTTP status code 400 Bad Request (please enter all fields)
    return Response(status=400)

# is Working
@app.route('/greenhouse/settings/soil-moisture/value', methods=['GET'])
def soil_moisture_settings_value():
    product_key = request.args.get('product_key')
    if product_key:
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = %s)', (product_key,))[0][0]:
            data = executeQueryData('SELECT SOIL_MOISTURE_ON, MIN_SOIL_MOISTURE FROM SETTINGS WHERE PRODUCT_KEY = %s', (product_key,), True)[0]
            return jsonify(data)
        # HTTP status code 404 Not Found (Greenhouse does not exist)
        return Response(status=404)
    # HTTP status code 400 Bad Request (please enter all fields)
    return Response(status=400)

# is Working
@app.route('/greenhouse/settings/soil-moisture/time', methods=['GET'])
def soil_moisture_settings_time1():
    product_key = request.args.get('product_key')
    interval = request.args.get('interval')
    if product_key:
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = %s)', (product_key,))[0][0]:
            query_select = 'SELECT SETTINGS.SOIL_MOISTURE_ON, TIMETABLE.TIMETABLE_ON, TIMETABLE.INTERVAL_TIME ,TIMETABLE.FROM_TIME FROM SETTINGS '
            query_join = 'INNER JOIN TIMETABLE ON SETTINGS.ID_SETTINGS = TIMETABLE.ID_SETTINGS '
            query_where = 'WHERE SETTINGS.PRODUCT_KEY = %s AND TIMETABLE.TIMETABLE_TYPE = "soilMoisture"'
            args = (product_key,)
            if interval:
                query_where = 'WHERE SETTINGS.PRODUCT_KEY = %s AND TIMETABLE.INTERVAL_TIME = %s AND TIMETABLE.TIMETABLE_TYPE = "soilMoisture"'
                args = (product_key, interval)
            data = executeQueryData(query_select + query_join + query_where, args, True)[0]
            return jsonify(data)
        # HTTP status code 404 Not Found (Greenhouse does not exist)
        return Response(status=404)
    # HTTP status code 400 Bad Request (please enter all fields)
    return Response(status=400)

# is Working
@app.route('/greenhouse/settings/light', methods=['GET'])
def light_settings():
    product_key = request.args.get('product_key')
    interval = request.args.get('interval')
    if product_key:
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = %s)', (product_key,))[0][0]:
            query_select = 'SELECT TIMETABLE.TIMETABLE_ON, TIMETABLE.INTERVAL_TIME, TIMETABLE.FROM_TIME, TIMETABLE.TO_TIME FROM SETTINGS '
            query_join = 'INNER JOIN TIMETABLE ON SETTINGS.ID_SETTINGS = TIMETABLE.ID_SETTINGS '
            query_where = 'WHERE SETTINGS.PRODUCT_KEY = %s AND TIMETABLE.TIMETABLE_TYPE = "light"'
            args = (product_key,)
            if interval:
                query_where = 'WHERE SETTINGS.PRODUCT_KEY = %s AND TIMETABLE.INTERVAL_TIME = %s AND TIMETABLE.TIMETABLE_TYPE = "light"'
                args = (product_key, interval)
            data = executeQueryData(query_select + query_join + query_where, args, True)[0]
            return jsonify(data)
         # HTTP status code 404 Not Found (Greenhouse does not exist)
        return Response(status=404)
    # HTTP status code 400 Bad Request (please enter all fields)
    return Response(status=400)

@app.route('/greenhouse/settings', methods=['GET'])
def settings():
    product_key = request.args.get('product_key')
    if product_key:
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM SETTINGS WHERE PRODUCT_KEY = %s)', (product_key,))[0][0]:
            query_select = 'SELECT * FROM SETTINGS '
            query_join = 'INNER JOIN TIMETABLE ON SETTINGS.ID_SETTINGS = TIMETABLE.ID_SETTINGS '
            query_where = 'WHERE SETTINGS.PRODUCT_KEY = %s'
            data = executeQueryData(query_select + query_join + query_where, (product_key,), True)
            return jsonify(data)
        # HTTP status code 404 Not Found (Greenhouse does not exist)
        return Response(status=404)
    # HTTP status code 400 Bad Request (please enter all fields)
    return Response(status=400)



# Creates a new user into the DB when recieve a get or post
# Is working
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
        response = None
        conn = connDB()
        cur = conn.cursor()
        hash_pw = sha256_crypt.encrypt(password)
        proc = cur.callproc('NEW_USER', (firstname, lastname, email, hash_pw, None))
        
        if proc[4] == 'Success':
            conn.commit()
            # HTTP status code 201 Created
            response = Response(status=201)
        else:
            # HTTP status code 409 Conflict (User already exist!)
            response = Response(status=409)
        cur.close()
        conn.close()
        
        return response
            
    else:
        # HTTP status  code 400 Bad Request (please enter all fields)
        return Response(status=400)

#  is working
@app.route('/user/information/update', methods=['GET', 'POST'])
def update_user():
    if request.method == 'POST':
        firstname = request.form.get('firstname')
        lastname = request.form.get('lastname')
        email = request.form.get('e-mail')
        new_email = request.form.get('new_e-mail')
        password = request.form.get('password')
        new_password = request.form.get('new_password')
    else:
        firstname = request.args.get('firstname')
        lastname = request.args.get('lastname')
        email = request.args.get('e-mail')
        new_email = request.args.get('new_e-mail')
        password = request.args.get('password')
        new_password = request.args.get('new_password')
        
    if email and firstname and lastname:
        print(password)
        print(new_password)
      
        response = None
        if password and new_password:
            if check_password(email, password):
                new_password = sha256_crypt.encrypt(new_password)
                print("Debug")
            else:
                # HTTP status code 403 Forbidden (false e-mail or password)
                return Response(status=403)
        if new_email == email:
            new_email = None
        conn = connDB()
        cur = conn.cursor()
        proc = cur.callproc('UPDATE_USER', (firstname, lastname, email, new_email, new_password, None))
        if proc[5] == 'Success':
            conn.commit()
            # HTTP status code 200 OK
            response = Response(status=200)
        else:
            # HTTP status code 404 Not Found (e-mail does not exist)
            response = Response(status=404)
        cur.close()
        conn.close()
        return response

    else:
        # HTTP status  code 400 Bad Request (please enter all fields)
        return Response(status=400)

# is working
@app.route('/user/login', methods=['POST'])
@cross_origin()
def user_login():
    email = request.form.get('e-mail')
    password = request.form.get('password')
    if email and password:
        print(email)
        print(password)
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM TBL_USER WHERE EMAIL = %s)', (email,))[0][0]:
            if check_password(email, password):
                # HTTP status code 200 OK
                return Response(status=200)
        # HTTP status code 403 Forbidden (false e-mail or password)
        return Response(status=403)
    # HTTP status  code 400 Bad Request (please enter all fields)
    return Response(status=400)

# is Working
@app.route('/user/information/first-name', methods=['GET'])
def user_first_name():
    if request.method == 'POST':
        email = request.form.get('e-mail')
    else:
        email = request.args.get('e-mail')

    if email:
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM TBL_USER WHERE EMAIL = %s )', (email,))[0][0]:
            return executeQueryData('SELECT FIRST_NAME FROM TBL_USER WHERE EMAIL = %s', (email,))[0][0]
        # HTTP status code 404 Not Found (e-mail does not exist)
        return Response(status=404)
    # HTTP status  code 400 Bad Request (please enter all fields)
    return Response(status=400)

# is Working
@app.route('/user/information/last-name', methods=['GET'])
def user_last_name():
    if request.method == 'POST':
        email = request.form.get('e-mail')
    else:
        email = request.args.get('e-mail')

    if email:
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM TBL_USER WHERE EMAIL = %s)', (email,))[0][0]:
            return executeQueryData('SELECT LAST_NAME FROM TBL_USER WHERE EMAIL = %s', (email,))[0][0]
        # HTTP status code 404 Not Found (e-mail does not exist)
        return Response(status=404)
    # HTTP status  code 400 Bad Request (please enter all fields)
    return Response(status=400)
    
@app.route('/user/information', methods=['GET'])
def user_information():
    if request.method == 'POST':
        email = request.form.get('e-mail')
    else:
        email = request.args.get('e-mail')

    if email:
        if executeQueryData('SELECT EXISTS (SELECT 1 FROM TBL_USER WHERE EMAIL = %s)', (email,))[0][0]:
            data = executeQueryData('SELECT LAST_NAME, FIRST_NAME FROM TBL_USER WHERE EMAIL = %s', (email,))
            return jsonify(data)
        # HTTP status code 404 Not Found (e-mail does not exist)
        return Response(status=404)
    # HTTP status  code 400 Bad Request (please enter all fields)
    return Response(status=400)

if __name__ == "__main__":
    app.run(host="0.0.0.0")
    