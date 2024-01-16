from database import Client
from dotenv import load_dotenv

import os
import sys

load_dotenv()

mongo_db = os.getenv('MONGO_DB')
mongo_uri = os.getenv('MONGO_URI')

required_vars = [mongo_db, mongo_uri]

for var in required_vars:
    if not var:
        sys.exit('Missing required variables! Check your .env file.')

client = Client(mongo_uri)

database = client.get_db(mongo_db)

user_collection = database.get_collection('user')
review_collection = database.get_collection('review')
voucher_collection = database.get_collection('voucher')
info_collection = database.get_collection('restaurantInfo')


user_data = [
    {
        "username": "alice",
        "name": "Alice Wonder",
    },
    {
        "username": "bob",
        "name": "Bob Da Builder",
    },
    {
        "username": "charlie",
        "name": "Charlie Wonka",
    },
    {
        "username": "Dona_Maria",
        "name": "Dona Maria"
    },
    {
        "username": "Senhor_Miguel",
        "name": "Senhor Miguel"
    }
]

restaurant_data = [
    {
        "data": {
            "restaurantInfo": {
            "address": "Rua da Glória, 22, Lisboa",
            "genre": [
                "Portuguese",
                "Traditional"
            ],
            "menu": [
                {
                "category": "Meat",
                "currency": "EUR",
                "description": "A succulent sirloin grilled steak.",
                "itemName": "House Steak",
                "price": 24.99
                },
                {
                "category": "Fish",
                "currency": "EUR",
                "description": "A Portuguese staple, accompanied by potatoes and salad.",
                "itemName": "Sardines",
                "price": 21.99
                },
                {
                "category": "Vegetarian",
                "currency": "EUR",
                "description": "Creamy Arborio rice cooked with assorted mushrooms and Parmesan cheese.",
                "itemName": "Mushroom Risotto",
                "price": 16.99
                }
            ],
            "owner": "Maria Silva",
            "restaurant": "Dona Maria",
            "username": "Dona_Maria"
            }
        },
        "metadata": {
            "nonce": "CXNWqCFqXDKFOUc5+yHLbr5sY+I\u003d",
            "timestamp": "21:23:04 - 19/12/2023"
        },
        "signature": "Q4glds150i1xzsndBD67iRbxMT5O4hq0u+eQyPSqIoDOXsW7JhwKQUJNvgBVekUk6Xa7hjPWcD9NHgk5tr6zXdriz5kr3SQ7QO7uYEPiM1b0t2f5nOOhmlB2xkLwVQ1z+fP7UrQfct5Qm4/bEAB/gle3TWFK6QdjvB3gxlO/vlECx4Fi9zPLdnnKZD24xnYeOPfMOIw9BG6kNRaMmcqxbDFWSiqMLv1BAAzcy/1BKbbb7We8hqKVIGYVuJHSOchic1VfrvLvpl+TcHyA1/H9s+mhsyS6/q9Wdx7japVUKB/CdiBXa/20EsLwbBiXHsIwXp0DQ+LSvDcMehFpFzXxsg\u003d\u003d"
    },
    {
        "data": {
            "restaurantInfo": {
            "address": "Rua da Rosa, 1700-321 Lisboa",
            "genre": [
                "Portuguese"
            ],
            "menu": [
                {
                "category": "Meat",
                "currency": "EUR",
                "description": "The classic portuguese lunch dish.",
                "itemName": "Bitoque",
                "price": 5.99
                }
            ],
            "owner": "Miguel Marciano",
            "restaurant": "Senhor Miguel",
            "username": "Senhor_Miguel"
            }
        },
        "metadata": {
            "nonce": "TjjjY+WB6S10HE5s092ZYnscyhQ\u003d",
            "timestamp": "21:23:28 - 19/12/2023"
        },
        "signature": "hI58zYY+74bo6JD0X87jDkEjAL6xdjoOcPhbSVDa6DcPmMTsb9xKPEKOYQiL9PUNqDmqyDxe+nPJIL8xubQMOaab5nP6ibitmYHZ89MIkOqRTCkfAFDONii5RdMRtnEednx15Hg8aZCRsxHCuaCc5Q1lSxJMeVzdYqlKitLBAIJQa4JwZoj+L/dvZX6yp+b7L4VeL8iCTBU82LMJKSSOb9+fv5nFRseDSA3kBLoCgPoKA1qUumbO1narfEgIv3Gxv0h2hBHiTY7uCZVi+vNB1EJEQNUbbXvBU7P6UoGJgFxp2bDkBgNhw8ahGxZhgxI0NlplDJZwhgbJNhx0/2/y5g\u003d\u003d"
    }
]

review_data = [
    {
        "data": {
            "comment": "Good service.",
            "rating": 3,
            "restaurant": "Dona Maria",
            "username": "alice"
        },
        "metadata": {
            "nonce": "16dmjFru7tmFMJ/Hxl0hfb0HUJk\u003d",
            "timestamp": "21:30:10 - 19/12/2023"
        },
        "signature": "Q/xm00ClLbGTHR8oMNUhmesmWCm0/g5T8K+cux7DwdjtvPsBfvo/5AKSK13zaHI3A1OxP3PhMk4nH8WZnZhgDB7Fk5QVViGJ/N5mvw+lMPkYICdGhuRxrEMyO3SVY1jrXORv2Opb7kcQaIkVqXWqrP9Gugq2foreH4XeHst4nG6OJYRrdeNnNpP7Hhp3PkxXIC0j8hlWMHzCecoZ4+qs/C8C0RSQFCc0ekmTvbrW9YMALP1fq+OZafJmD+e0E2Kjs7Jyi9iLAjTHQ1ngZHm6PUIhwuuP8CSzr1sr+fY0gwDT7Lbm7wPXDCmD4V2ScAi3GgCeOz+yUxek3YQ2qFje/g\u003d\u003d"
    },
    {
        "data": {
            "comment": "Amazing food!",
            "rating": 4,
            "restaurant": "Dona Maria",
            "username": "bob"
        },
        "metadata": {
            "nonce": "hOyNdzE5YXEo8C7C6svtB1uOFJs\u003d",
            "timestamp": "21:30:25 - 19/12/2023"
        },
        "signature": "liH7exOUUgYl3OvpValF55jwy4/jTy0ezM/SNZV3z4JkWgjMskvuKHxcAAr2uONwciSRCo7MCPaPNvMo9wSFeTqGrKw5UKi3p+u2MTiIADdwwGk0UPu+CW3nZ5HsHWtth6o+KRBDr6JaffYuXhFl566F8DQ0Jg0IzNWRxFkxcvnomrz77WOfS9roLub0zzJNFMghEMnodphPMf+aluNLYVaCaZ58elFggJefURHriLYEgJY2hR81r1YXmiHvnu1nYh2qJ2sB9uL1ZqL8ohLEjaSNctlt3snZaIw5ehegJQpGjcv+ZXg41c4EFfTbTZrKgTbbsU2wUiLAY7oOXFWYfQ\u003d\u003d"
    },
    {
        "data": {
            "comment": "Great experience!",
            "rating": 5,
            "restaurant": "Dona Maria",
            "username": "charlie"
        },
        "metadata": {
            "nonce": "2I2FTP6/xxvjpDLesic3hN3t+Y4\u003d",
            "timestamp": "21:30:37 - 19/12/2023"
        },
        "signature": "CTVtcJuuw8wMHrHp5nh5kj4i9kH2tCAym3tV+pQbKuWWRblLIbNQKrGMqx/3sfAMmJpt9w1UcpLVDL7wMjz8dmCneZZcznyjmTxbNZKUby9+V/kmYc4pD/hxVGaxbWUk939NNBJyRJf/KcxxoCPqqal/oyvkmRyMtpCE0RCmxXsZlnu53QcpP7JH76BzPqRe6DoNkEUJJUHWre7EjyTmqnBTeBe8uLfooVJ19bESyf75yctNJmoumIEHY5XuDutWEQ5tVTLxVjRyUdiw1ZckcQmgCebG4Qeg5LkV4QLpc0p9zjoqN+7TAEE2WgMGG7N9LSLW449rvk+mCuEQQrswNA\u003d\u003d"
    }
]

voucher_data = [
    {
        "data": {
            "mealVoucher": "B6mU6fEnQNGPP/sRNcjbHqm1pzhrLfi67kAMIX0lqv2ycHSGRqh0TYYKm1UJ0vTMGtife5U9PqhkbAhzhZOi8j+RKt5GIhbVcXaR6JxxzvMLHKaGDbLUff/3wINDby8fFULkGQ8mEKJqY6RQpKizceIxni2EQ4sC8gph/WbkuFW08kz7+51Rw6hoXclYbE4BmcPsmAWmoQkX+D9Cj3iv34Z4LM74XgX9jCetRLJvXyiOPFC5zIIk9qqPVNH/ajCWTVspNVfoYiZGKTuiA5UvKAdn8IZUt8UQmVwjrkdl30gxFaOS37E0zsDsJdIwdfkCIzpwv6GRCF4BFDpV1iHz/g\u003d\u003d",
            "owner": "alice",
            "previousOwner": "Dona Maria",
            "restaurant": "Dona Maria"
        },
        "metadata": {
            "nonce": "1R+DAx5VBpxhqQCjt+quykc4Te8\u003d",
            "timestamp": "21:27:03 - 19/12/2023",
            "voucherSignature": "L9c7Kl0AfzCncui+WPeBuNqt4QN/EVghKIEqhGmoayzLrULxe8oHNDSz7n34JG44wW902kQ1mtsqwnRYm05TWaMggb7KX9ugHRlfyh3KZEHCxfnsD8bmWjK82jg4qyOHNtlBNKJ4U3tfgAxtQZcvHTEuVHsqPJ+8fLpwhsTvTCKm4BG20dguiAVKDSE2z35gY9ctQg15bhrQQLg2i7BEmNM7RRghtEWEdEsirWP0Sd7tVkyFezlG49EACr0APBmxXg1SzFgecBwPTCJCrd1v5pJNfyDR+VN0H2IJbLHhfZfS/1Gu0IhTo+YrHNcZ+a+gnAsVxpjetIOMP6yCUCV9Sg\u003d\u003d"
        },
        "signature": "OX+JUqNERPnI2quZ6HIqZaHJT9t9QdcE/iZVEmJnRvMCcP3XF9uqkxqopQXj5XDCDdvybqK9cvpPWHvor315PWrgAUy027Mzna04fybLECscj78gDTuwCkCXRr7My/bHzIIbkOdR03neVOUVVDVv0Qkmv30PZ+tPH2hbsNExPLXvUhDiF9VjdBzpFeDGU0prHjBqXBCqeGP59brKlaJNwTtNGtYqroIgvx+PIqDqZlDkaqz1O+pTrUl8vGF2WGzqDK4ilF0toxX7+2kP+OWkQ/mV7swo42Fc2J7xcAL2GNiEzoNtunLpR+7LhJviz0u46OiNaKcFfcCSPBodGFrbbQ\u003d\u003d"
    },
    {
        "data": {
            "mealVoucher": "KZgnI/DPsAKKiC/JlSbZZSSSEUdwCaVJgaOtWLufPZOu4DaZqMWxwHP/I0PgHl8G+4NYSRoyD6q+idcZ6B2o2y5yLozPD/MRF5hfk16qF2g4wm/aCBiaZfggvHBm9lh/awbmUkpWhiSSIndOyKSh1wab0U/wGZvy2/65WwBEVf2KjOCvEzL83LNGWtHmf8Rse78ykB0EW3Lsw/eTcb2WVp1DuUh9At4CGkDvrD8Fth6KJOu9Ei8P+mEc5KdwuzjHnQKWk1x1k/CnePLUqnGB7FdtgRCts+E2SabyY5/10PAn/aTD2n00fl0cnwIyZ9YOjA4Fb3W/Me3OQQYfZGvaGg\u003d\u003d",
            "owner": "alice",
            "previousOwner": "Dona Maria",
            "restaurant": "Dona Maria"
        },
        "metadata": {
            "nonce": "wIwIscR7cWEA4rHtV9QA/kIWJPc\u003d",
            "timestamp": "21:27:18 - 19/12/2023",
            "voucherSignature": "CbbZJW/ZcPIFzAGxGX/KlpYstulHC28V+me6W+UsTzLogSyQOq5C+xwNarkQLrUqWqVLsZVRs/5KN7h7TY3aN+pQRxAwtsmvjIUA3FF9hY9IQ8XV57IMlQzUBpaSOWzYFIO88YlQuVMLW15hdx+VU1nzl6wE4aMlpxXe8Up3SDDuBqDAXFVhKPtIFkqAML2zgKRA1tzYN44ozT0PjSn2aS8KdWSmB/AkvAYqIvaYdYeGRcJ05hoyD7jAKo1IWEZRcTE27Xc/owoUXMDBAvVk+hsI+gNspEQWynWfhFY4mD2FYUCy8sOtE5qYsL7KaD10qsY1NS6EpaIApPoTnYvbEw\u003d\u003d"
        },
        "signature": "c1s+ZiLaWjccn6S+BR2LXEOlArFn5FULijwNQX1Wfr6ioQRvXue2zSewxxyW7YO1aVpjG+ANfRVdVauco2dM8D5mCHGvxFH9eCfXQBzFYr4sxH/csV5uAgjj8ebi8aqNNar96Pfj1XT6u1prDAo/N6pDwAJy6HlmxsAxBkODAVxRCjGw2sViv94mlcnQ6FAe8XVZ2eQACxWRAvTMq0DJu+EG3wp8g9uTny+WPrtjHH1J3HNXXIIJpEo3dcFm0qXKmMKZEGM420CiKaCFNqnorWlm6eWQXX2rfylUsv3bcWb1vhrc6+U+D11NZAW0dlnwJYh7RlzVBqsyR52wJglMYA\u003d\u003d"
    },
    {
        "data": {
            "mealVoucher": "lYZJW6+ZTtfc6+od0w2rB5/1X2izareMWJD83xEipGNdl6TGOMOa5t8slRCi0QOgt0O2zpBMc5aIJNBhlFcYyuL8KtxMbncaVVzrRIDvMSUa/cK6y4qKKFVjPbQ1unag9PjU08XdbonNxUG2x7KPyzMylNKHCvU6HT0aPT27pliOXpXFeiTBmHBTvMFrnT6LzNJ1SC0bimixc/QHq0SSlW8dUUo5PLry6YFcalLfdrlL5XQgp9AqrReynhwNQ3cstg8Df4enggkKjRgEB+avZNUi8zy4mmBubHm4NHvOiLkAVlye6zu2o8w3jB5Hy8zTBjZ0Qv7v/Fy0no+Jdr0TMA\u003d\u003d",
            "owner": "bob",
            "previousOwner": "Dona Maria",
            "restaurant": "Dona Maria"
        },
        "metadata": {
            "nonce": "jXvH72snTrVkJmlb6mxWx2fMs5M\u003d",
            "timestamp": "21:27:37 - 19/12/2023",
            "voucherSignature": "fvqLcHCPeI8K4bkMdPUUmtic+bwkE8rb3gCRlGUMxIjZ+R1N4zDBPzOoubKtnNdfEgHFOfAUHpJcrbgS+hQo0rW1ELbSpmcGOCQ4jMUc77kdXMAPkZ9Fj7a0sUXBWItmdimAt7kYpz3kRffknt/Lzvo4uffK1KinpStWCDdMevBGGqS9hk1qsmZU8mhT8R4U5tGKC8zfa+bImAKq6VcqD5LfJyqnC6nMAJzZ3wMC5KBzwP1dfUuQ4kxu5Cb6eGd0uC3Wrrf4HQq7eiJElnAhEdffWUvmAWrbDY26EFm08Kdv+0ieOI2j3ZKy9v+pwzv0avmO1fE4PrT3HaDLrROoFA\u003d\u003d"
        },
        "signature": "VOepBjAo0GbPHliBmb6tp4PhhwmW98z5smzE0mWo5NoCmwKf46rfFqc90d/BCvahi4sViHdScem7gwRNv6TYJCiACiX8AkX4AIEEeZ2FnfyeZpARGBPQF2BlyBWKUKA6qkasyZHvrqfe5/n/5dwp0H413xQQd7lFGLxhxT4GLuw0KpGJ5grYX4Aq+YZ8rkNdzag3rR+lvcAkbthdU1DUl2FRHhZ4m51rLpUzD0m16w9YIb2HVF7J+SFIl4hgRmuyv1Mu+LJegDNk4djc+43EaYcvIQSo2lh1BKWsZ3pBcoP3MytRKuBDhmV1tREAg6pLOiEIQZ0CoQkqBzp6wPKzFg\u003d\u003d"
    }
]

# Insert data into the collection
results = []

results.append(user_collection.insert_many(user_data))
results.append(review_collection.insert_many(review_data))
results.append(voucher_collection.insert_many(voucher_data))
results.append(info_collection.insert_many(restaurant_data))

if None in results:
    print("Error inserting data.")

print("Data inserted successfuly")
client.close()