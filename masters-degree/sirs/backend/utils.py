from bson.json_util import loads

# Read Json file
def read_json_file(file_path):
    try:
        with open(file_path, 'r') as file:
            return loads(file.read())
    
    except Exception:
        return None