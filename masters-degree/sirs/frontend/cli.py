import requests, subprocess, json, os


CA_CERT_PATH = "../auth/certificates/ca.crt"
HOST = "https://bombappetit:5000"


def write_file(item, filename):
    with open(filename, 'w', encoding='utf-8') as file:
        json.dump(item, file, indent=2, ensure_ascii=False)

def remove_file(file):
    if os.path.exists(file):
        os.remove(file)

def read_json(filename):
    with open(filename, 'r') as file:
        data = json.load(file)

    return data


def get_info(info):
    response = requests.get(f"{HOST}/{info}", verify=CA_CERT_PATH)
    
    if response.status_code == 200:
        return response.json()


def post_info(info, data):
    response = requests.post(f"{HOST}/{info}", json=data, verify=CA_CERT_PATH)
    
    if response.status_code == 200:
        return response.json()


def put_info(info, data):
    response = requests.put(f"{HOST}/{info}", json=data, verify=CA_CERT_PATH)

    if response.status_code == 200:
        return response.json()


def delete_info(info):
    response = requests.delete(f"{HOST}/{info}", verify=CA_CERT_PATH)

    if response.status_code == 200:
        return response.json()
    
def check(item, is_voucher=False, is_review=False, is_info=False):
    write_file(item, "tmp.json")

    if is_voucher:
        signer = item["data"]["previousOwner"].lower().replace(' ', '_')

    elif is_review:
        signer = item["data"]["username"]

    elif is_info:
        signer = item["data"]["restaurantInfo"]["username"].lower()

    result = subprocess.run(["secdoc", "check", f"../auth/certificates/trusted_keys/{signer}_public.key", "tmp.json"], stdout=subprocess.PIPE)

    if b"not fresh" in result.stdout:
        print("Document is not fresh, exiting")
        exit()

    if b"has been modified" in result.stdout:
        print("Document has been tampered, exiting")
        exit()
    
    remove_file("tmp.json")
    

def unprotect(item, is_voucher=False):
    write_file(item, "tmp.json")

    if is_voucher:
        result = subprocess.run(["secdoc", "unprotect", "-e", f"../auth/keys/{user}_private.key", "tmp.json", "out.json"], stdout=subprocess.PIPE)
    else:
        result = subprocess.run(["secdoc", "unprotect", "tmp.json", "out.json"], stdout=subprocess.PIPE)

    output = read_json("out.json")
    remove_file("tmp.json")
    remove_file("out.json")
    return output


def protect(itemProtected, itemUnprotected, new_owner):
    write_file(itemProtected, "tmp.json")
    write_file(itemUnprotected, "tmp2.json")

    result = subprocess.run(
        [
            "secdoc", "protect", "--voucher", "-t", 
            f"../auth/certificates/trusted_keys/{new_owner}_public.key",  # new owner's key to encrypt
            f"../auth/keys/{user}_private.key",   # previous owner key to sign
            "tmp.json",  # item original and protected 
            "tmp2.json",  # item original and unprotected
            "out.json"
            ], 
        stdout=subprocess.PIPE
    )

    output = read_json("out.json")
    remove_file("tmp.json")
    remove_file("tmp2.json")
    remove_file("out.json")
    return output

def protect_review(item):
    write_file(item, "tmp.json")

    result = subprocess.run(
        [
            "secdoc", "protect", "--review", 
            f"../auth/keys/{user}_private.key",
            "tmp.json",
            "out.json"
            ], 
        stdout=subprocess.PIPE
    )

    output = read_json("out.json")
    remove_file("tmp.json")
    #remove_file("out.json")
    return output


def start_menu():
    users = {}
    response = get_info("users/")
    n_users = 1

    print("Choose an user: ")

    for user in response["Users"]:
        if "_" in user['username']:
            continue
        user_name = user['username']
        users[n_users] = user_name
        print(f"{n_users}. {user_name}")
        n_users += 1

    print("0. Exit")
    
    user_choice = int(input("Choice: "))
    
    if (user_choice == 0):
        print("Exiting...")
        exit()

    user  = users[user_choice]
    print("Chosen user: " + user + '\n')

    return user


def user_menu():
    choices = {1: "Restaurant Info", 2: "Vouchers", 3: "Reviews"}
    print("\nMain menu: ")

    for key in choices:
        print(f"{key}. {choices[key]}")

    user_choice = int(input("Choice: "))
    
    return user_choice


def list_restaurants():
    restaurants = {}
    response = get_info("restaurants/")
    n_restaurants = 1

    print("\nChoose a restaurant:")
    for restaurant in response:
        check(restaurant, is_info=True)
        r = unprotect(restaurant)

        restaurants[n_restaurants] = r
        print(f"{n_restaurants}. {restaurants[n_restaurants]['restaurantInfo']['restaurant']}")
        n_restaurants += 1

    print("0. Exit")
    
    user_choice = int(input("Choice: "))
    if (user_choice == 0):
        print("Exiting...")
        exit()
    
    return restaurants[user_choice]


def restaurant_info():
    print("\nWhich restaurant info do you want to check?")
    restaurant  = list_restaurants()

    info = restaurant["restaurantInfo"]
    print("Owner: " + info["owner"])
    print("Restaurant name: " + info["restaurant"])
    print("Genre: ")
    for g in info["genre"]:
        print("\t" + g)
    
    print("Menu: ")
    for m in info["menu"]:
        print("\tName: " + m["itemName"])
        print("\tCategory: " + m["category"])
        print("\tDescription: " + m["description"])
        print("\tPrice: " + str(m["price"]))
        print("\tCurrency: " + m["currency"])
        print("\n")


def vouchers_menu():
    print("\nVouchers: ")
    choices = {1: "Transfer voucher", 2: "Use voucher", 3: "List vouchers"}

    for key in choices:
        print(f"{key}. {choices[key]}")

    user_choice = int(input("Choice: "))

    if (user_choice == 1):
        transfer_voucher()

    elif (user_choice == 2):
        use_voucher()
        
    elif (user_choice == 3):
        list_vouchers()


def reviews_menu():
    print("\nReviews: ")
    choices = {1: "Create review", 2: "List reviews"}

    for key in choices:
        print(f"{key}. {choices[key]}")
    
    user_choice = int(input("Choice: "))

    if (user_choice == 1):
        create_review()

    elif (user_choice == 2):
        list_reviews()


def list_vouchers(is_transfer=False):
    vouchers = {}
    response = get_info(f"vouchers/{user}")
    n_vouchers = 1

    print("\nChoose a voucher: ")
    for voucher_protected in response:

        check(voucher_protected, is_voucher=True)
        voucher = unprotect(voucher_protected, is_voucher=True)

        if is_transfer:
            vouchers[n_vouchers] = (voucher, voucher_protected)
        else:
            vouchers[n_vouchers] = voucher
        
        print(f"{n_vouchers}.\tRestaurant: {voucher['restaurant']}\n  \tCode: {voucher['mealVoucher']['code']}\n  \tDescription: {voucher['mealVoucher']['description']}\n")
        n_vouchers += 1

    print("0. Exit")
    
    user_choice = int(input("Choice: "))
    if (user_choice == 0):
        print("Exiting...")
        exit()

    return vouchers[user_choice]

def list_users(dont_change_user=False):
    global user
    users = {}
    response = get_info("users/")
    n_users = 1

    print("\nChoose an user: ")

    for usr in response["Users"]:
        if usr["username"] == user or "_" in usr["username"]:
            continue
        user_name = usr["username"]
        users[n_users] = user_name
        print(f"{n_users}. {user_name}")
        n_users += 1

    print("0. Exit")
    
    user_choice = int(input("Choice: "))

    print("Chosen user: " + users[user_choice] + '\n')

    if dont_change_user:
        return users[user_choice]
    user  = users[user_choice]

    return user


def create_review():
    print("What restaurant do you want to review?")
    restaurant = list_restaurants()
    rating = int(input("Your rating: "))
    comment = input("Your comment: ")
    review = {"comment": comment, "rating": rating, "restaurant": restaurant["restaurantInfo"]["restaurant"], "username": user}
    print(post_info("review/", protect_review(review)))


def list_reviews():
    print("What reviews do you want to see?")
    restaurant = list_restaurants()
    print(f"restaurant/{restaurant['restaurantInfo']['username']}/reviews")
    response = get_info(f"restaurant/{restaurant['restaurantInfo']['username']}/reviews")
    for r in response:
        check(r, is_review=True)
        r = unprotect(r)

        print("Username: ", r["username"])
        print("Rating: ", r["rating"])
        print("Comment: ", r["comment"])
        print("\n")


def transfer_voucher():
    print("Which voucher do you want to transfer?")
    vouchers = list_vouchers(is_transfer=True)
    print("Which user do you want to transfer your voucher to?")
    dst = list_users(dont_change_user=True)

    vouchers[0]["previousOwner"] = vouchers[0]["owner"]
    vouchers[0]["owner"] = dst

    update_voucher = protect(vouchers[1], vouchers[0], dst)

    vouchers_db = get_info("vouchers/")

    for v in vouchers_db["Vouchers"]:
        if v["metadata"]["voucherSignature"] == vouchers[1]["metadata"]["voucherSignature"]:
            print(put_info(f"voucher/{v['_id']['$oid']}", update_voucher))
            return


def use_voucher():
    vouchers = list_vouchers(is_transfer=True)
    vouchers_db = get_info("vouchers/")

    for v in vouchers_db["Vouchers"]:
        if v["metadata"]["voucherSignature"] == vouchers[1]["metadata"]["voucherSignature"]:
            delete_info(f"voucher/{v['_id']['$oid']}")
            return


def main():
    global user
    user = start_menu()
    user_choice = None

    while user_choice != 0:    
        user_choice = user_menu()
        if (user_choice == 1):
            restaurant_choice = restaurant_info()

        elif (user_choice == 2):
            voucher_choice = vouchers_menu()

        elif (user_choice == 3):
            reviews_choice = reviews_menu()


if __name__ == "__main__":
    main()
