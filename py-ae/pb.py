import json

file_path = "data/phonebook.json"


def load_phone_book(file):
    try:
        with open(file, 'br') as h:
            return json.loads(h.read())
    except (FileNotFoundError, json.JSONDecodeError):
        print(f"Could not load {file}, creating an empty phone book.")
        return {}


def save_phone_book(phone_book, file):
    with open(file, 'wb') as h:
        h.write(json.dumps(phone_book).encode("utf8"))


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
    phone_book = load_phone_book(file_path)
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
                save_phone_book(phone_book, file_path)
                print("Phone book saved. Goodbye!")
                break
            case _:
                print("Invalid choice. Please enter 1, 2, or 3.")


if __name__ == "__main__":
    main()
