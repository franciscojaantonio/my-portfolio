from pymongo import MongoClient, errors
from bson.objectid import ObjectId
import sys

class Client:

    # Connect to MongoDB
    def __init__(self, mongo_uri):
        try:
            self.client = MongoClient(mongo_uri)
        
        except errors.ConfigurationError:
            sys.exit('Mongo URI not set or incorrect!')

        self.check_client_connection()

    # Send a ping to confirm a successful connection
    def check_client_connection(self):
        try:
            self.client.admin.command('ping')

        except Exception as e:
            sys.exit(f'Could not connect to MongoDB: {e}')

        print('Successfully connected to MongoDB!')

    # Get a database by its name
    def get_db(self, db_name):
        try:
            db = self.client[db_name]

        except errors.ConfigurationError:
            sys.exit(f"Database '{db_name}' not set or incorrect!")

        return Database(db)

    # Close the connection to MongoDB
    def close(self):
        self.client.close()

class Database:

    # Connect to MongoDB
    def __init__(self, database):
        self.database = database

    # Get a collection of the database by its name
    def get_collection(self, collection_name):
        try:
            collection = self.database[collection_name]

        except errors.ConfigurationError:
            sys.exit(f"Collection '{collection_name}' not set or incorrect!")

        return Collection(collection)

class Collection:

    # Get a collection from a database
    def __init__(self, collection):
        self.collection = collection

    # Insert a document in the collection
    def insert_document(self, document):
        try:
            document_id = self.collection.insert_one(document).inserted_id

        except errors.PyMongoError as e:
            sys.exit(f'Could not insert document: {e}')

        return document_id
    
    # Insert many documents to a collection
    def insert_many(self, documents):
        try:
            ids = self.collection.insert_many(documents)

        except errors.PyMongoError as e:
            sys.exit(f'Could not insert documents: {e}')

        return ids
    
    # Get a document by its ID
    def get_document_by_id(self, document_id):
        try:
            document = self.collection.find_one({'_id': ObjectId(document_id)})

        except errors.PyMongoError as e:
            sys.exit(f'Could not get document: {e}')

        return document
    
    def get_document_by_field(self, field, value):
        try:
            document = self.collection.find_one({field: value})

        except errors.PyMongoError as e:
            sys.exit(f'Could not get document: {e}')

        return document

    # Get all documents from the collection
    def get_documents(self):
        try:
            documents = self.collection.find()

        except errors.PyMongoError as e:
            sys.exit(f'Could not get documents: {e}')

        return list(documents)
    
    # Update a document by its ID
    def update_document(self, document_id, update):
        try:
            self.collection.update_one(
                {'_id': ObjectId(document_id)}, 
                {'$set': {
                    "data": update["data"], 
                    "metadata": update["metadata"], 
                    "signature": update["signature"]
                    }
                }
            )

        except errors.PyMongoError as e:
            sys.exit(f'Could not update document: {e}')

        return True
    
    # Remove a document from the collection by its ID
    def remove_document(self, document_id):
        try:
            self.collection.delete_one({'_id': ObjectId(document_id)})

        except errors.PyMongoError as e:
            sys.exit(f'Could not remove document: {e}')

        return True