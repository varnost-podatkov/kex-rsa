import json
import os

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

file_path = "../data/phonebook.bin"
ITERATIONS = 1000000


def derive_key(password, salt):
    kdf = PBKDF2HMAC(algorithm=hashes.SHA256(), length=16, salt=salt, iterations=ITERATIONS)
    return kdf.derive(password.encode("utf8"))


def load_phone_book(file, password):
    try:
        with open(file, 'br') as h:
            data = h.read()
        salt, iv, ct = data[:16], data[16:32], data[32:]
        key = derive_key(password, salt)

        gcm = AESGCM(key)
        pt = gcm.decrypt(iv, ct, None)

        return json.loads(pt)

    except Exception as e:
        print(f"Could not load '{file}', reason: '{e}'. Creating an empty phone book")
        phone_book = {}
    return phone_book


def save_phone_book(phone_book, file, password):
    pt = json.dumps(phone_book).encode("utf8")
    iv = os.urandom(16)
    salt = os.urandom(16)
    key = derive_key(password, salt)

    gcm = AESGCM(key)
    ct = gcm.encrypt(iv, pt, None)

    with open(file, 'wb') as file:
        file.write(salt + iv + ct)


def add_contact(phone_book, name, number):
    phone_book[name] = number
    print(f'Contact {name} added with number {number}.')


def search_contact(phone_book, query):
    hits = [(name, number) for name, number in phone_book.items() if name.find(query) != -1]

    if hits:
        print(f"Found {len(hits)} hits:")
        for name, number in hits:
            print(f"- {name}: {number}")
    else:
        print(f"No entries for query '{query}'")


def main():
    password = input("Password: ")

    phone_book = load_phone_book(file_path, password)
    print(f"Found {len(phone_book)} contacts.")

    while True:
        print("\nPhone Book Menu:")
        print("1. Add Contact")
        print("2. Search Contact")
        print("3. Exit")

        choice = input("Enter your choice (1/2/3): ")

        match choice:
            case '1':
                name = input("Enter contact name: ")
                number = input("Enter contact number: ")
                add_contact(phone_book, name, number)
            case '2':
                name = input("Enter contact name to search: ")
                search_contact(phone_book, name)
            case '3':
                save_phone_book(phone_book, file_path, password)
                print("Phone book saved. Goodbye!")
                break
            case _:
                print("Invalid choice. Please enter 1, 2, or 3.")


if __name__ == "__main__":
    main()
