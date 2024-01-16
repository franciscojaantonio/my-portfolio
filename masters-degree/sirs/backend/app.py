from flask import Flask, request, render_template, request
from bson.json_util import dumps
from utils import read_json_file
from dotenv import load_dotenv
from database import Client
import sys
import os

# Load environment variables from .env file
load_dotenv()

port = os.getenv('SERVER_PORT')
host = os.getenv('SERVER_HOST')

mongo_db = os.getenv('MONGO_DB')
mongo_uri = os.getenv('MONGO_URI')

template_path = os.getenv('TEMPLATE_PATH')

required_vars = [port, host, mongo_db, mongo_uri, template_path]

for var in required_vars:
    if not var:
        sys.exit('Missing required variables! Check your .env file.')

# Create the Flask app
app = Flask(__name__, template_folder=template_path)

# Database and Collections
client = Client(mongo_uri)

database = client.get_db(mongo_db)

user_collection = database.get_collection('user')
review_collection = database.get_collection('review')
voucher_collection = database.get_collection('voucher')
info_collection = database.get_collection('restaurantInfo')

''' Routes '''

# Index
@app.route('/', methods = ['GET'])
def index():
    return '<h1>Nothing to see here!</h1>'

''' Restaurant Info '''

# GET: Get a restaurant by name
@app.route('/restaurant/<string:restaurant_name>', methods = ['GET'])
def get_restaurant_by_id(restaurant_name):
    info = info_collection.get_document_by_field('restaurantInfo.username', restaurant_name)

    if not info:
        return 'Restaurant not found!', 404

    return dumps({'Restaurant': info})

# GET: Get a restaurant by name
@app.route('/restaurant/name/<string:restaurant_name>', methods = ['GET'])
def get_restaurant_by_name(restaurant_name):
    info = info_collection.get_document_by_field('restaurantInfo.restaurant', restaurant_name)

    if not info:
        return 'Restaurant not found!', 404

    return dumps({'Restaurant': info})

# GET: Get all restaurants
@app.route('/restaurants/', methods = ['GET'])
def get_restaurants():
    restaurants = info_collection.get_documents()

    if not restaurants:
        return dumps({'Restaurants': []})
    
    #return dumps({'Restaurants': restaurants})
    for r in restaurants:
        r.pop("_id")
    return restaurants

# GET: Get restaurant reviews
@app.route('/restaurant/<string:restaurant_name>/reviews', methods = ['GET'])
def get_restaurant_reviews(restaurant_name):
    info = info_collection.get_document_by_field('data.restaurantInfo.username', restaurant_name)

    if not info:
        return 'Restaurant not found!', 404

    reviews = review_collection.get_documents()

    if not reviews:
        return dumps({'Reviews': []})
    
    restaurant_reviews = []

    for review in reviews:
        if review['data']['restaurant'] == info['data']["restaurantInfo"]["restaurant"]:
            review.pop("_id")
            restaurant_reviews.append(review)
    
    # return dumps({'Reviews': restaurant_reviews})
    return restaurant_reviews

# GET: Get restaurant vouchers
@app.route('/restaurant/<string:restaurant_id>/vouchers', methods = ['GET'])
def get_restaurant_vouchers(restaurant_id):
    info = info_collection.get_document_by_id(restaurant_id)

    if not info:
        return 'Restaurant not found!', 404

    vouchers = voucher_collection.get_documents()

    if not vouchers:
        return dumps({'Vouchers': []})
    
    restaurant_vouchers = []

    for voucher in vouchers:
        if voucher['restaurant_id'] == restaurant_id:
            restaurant_vouchers.append(voucher)
    
    return dumps({'Vouchers': restaurant_vouchers})

''' Vouchers '''

# GET: Get a voucher by id
@app.route('/voucher/<string:voucher_id>', methods = ['GET'])
def get_voucher_by_id(voucher_id):
    voucher = voucher_collection.get_document_by_id(voucher_id)

    if not voucher:
        return 'Voucher not found!', 404

    return dumps({'Voucher': voucher})

# GET: Get vouchers of a user
@app.route('/vouchers/<string:username>', methods = ['GET'])
def get_vouchers_by_user(username):
    vouchers = voucher_collection.get_documents()

    if not vouchers:
        return dumps({'Vouchers': []})
    
    user_vouchers = []
    for voucher in vouchers:
        if voucher["data"]['owner'] == username:
            voucher.pop("_id")
            user_vouchers.append(voucher)
    
    return user_vouchers

# GET: Get all vouchers
@app.route('/vouchers/', methods = ['GET'])
def get_vouchers():
    vouchers = voucher_collection.get_documents()

    if not vouchers:
        return dumps({'Vouchers': []})
    
    return dumps({'Vouchers': vouchers})


# PUT: Update a voucher
@app.route('/voucher/<string:voucher_id>', methods = ['PUT'])
def update_voucher(voucher_id):
    voucher = voucher_collection.get_document_by_id(voucher_id)
    print(voucher)

    if not voucher:
        return 'Voucher not found!', 404

    voucher_collection.update_document(voucher_id, request.get_json())

    updated_voucher = voucher_collection.get_document_by_id(voucher_id)
    updated_voucher.pop("_id")
    return updated_voucher

    #return dumps({'Updated Voucher': voucher_collection.get_document_by_id(voucher["_id"])})

# DELETE: use a voucher
@app.route('/voucher/<string:voucher_id>', methods = ['DELETE'])
def use_voucher(voucher_id):
    voucher = voucher_collection.get_document_by_id(voucher_id)

    if not voucher:
        return 'Voucher not found!', 404

    voucher_collection.remove_document(voucher_id)

    return dumps({'Used Voucher': voucher})

''' Reviews '''

# GET: Get a review by id
@app.route('/review/<string:review_id>', methods = ['GET'])
def get_review_by_id(review_id):
    review = review_collection.get_document_by_id(review_id)

    if not review:
        return 'Review not found!', 404

    return dumps({'Review': review})

# GET: Get all reviews by user
@app.route('/reviews/<string:username>', methods = ['GET'])
def get_reviews_by_user(username):
    reviews = review_collection.get_documents()

    if not reviews:
        return dumps({'Reviews': []})
    
    user_reviews = []
    for review in reviews:
        if review['username'] == username:
            user_reviews.append(review)

    return dumps({'Reviews': user_reviews})

# GET: Get all reviews
@app.route('/reviews/', methods = ['GET'])
def get_reviews():
    reviews = review_collection.get_documents()

    if not reviews:
        return dumps({'Reviews': []})
    
    return dumps({'Reviews': reviews})

# POST: Add a review
@app.route('/review/', methods = ['POST'])
def add_review():
    data = request.json

    print(data)

    if not data:
        return 'No data provided!', 400
    
    review_id = review_collection.insert_document(data)

    if not review_id:
        return 'Review not inserted!', 400
    
    return dumps({'Review ID': review_id})

# PUT: Update a review
@app.route('/review/<string:review_id>', methods = ['PUT'])
def update_review(review_id):
    review = review_collection.get_document_by_id(review_id)

    if not review:
        return 'Review not found!', 404

    review_collection.update_document(review_id, request.json)

    return dumps({'Updated Review': review_collection.get_document_by_id(review_id)})

# DELETE: Remove a review
@app.route('/review/<string:review_id>', methods = ['DELETE'])
def delete_review(review_id):
    review = review_collection.get_document_by_id(review_id)

    if not review:
        return 'Review not found!', 404

    review_collection.remove_document(review_id)

    return dumps({'Removed Review': review})

''' Users '''

# GET: Get a user by username
@app.route('/user/<string:username>', methods = ['GET'])
def get_user_by_username(username):
    user = user_collection.get_document_by_id(username)

    if not user:
        return 'User not found!', 404

    return dumps({'User': user})

# GET: Get all users
@app.route('/users/', methods = ['GET'])
def get_users():
    users = user_collection.get_documents()

    if not users:
        return dumps({'Users': []})
    
    return dumps({'Users': users})

# Run the app
if __name__ == '__main__':
    # app.run(debug=True, host=host, port=port)
    app.run(debug=True, host=host, port=port, ssl_context=('../auth/certificates/server.crt', '../auth/keys/server_private.pem'))