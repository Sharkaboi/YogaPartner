import wikipediaapi
import requests
from bs4 import BeautifulSoup
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from lxml import etree

url = 'https://en.wikipedia.org/wiki/List_of_asanas'
page = requests.get(url)
soup = BeautifulSoup(page.text, 'html.parser')
dom = etree.HTML(str(soup))
table1 = dom.xpath("/html/body/div[3]/div[3]/div[5]/div[1]/table[2]/tbody/tr")
docs = []
for tr in table1[1]:
    i = 0
    for td in tr:
        print(td)
        if i == 0:
            url = r"https://en.wikipedia.org/" + td.get('href')
            print(url)
            wiki_wiki = wikipediaapi.Wikipedia('en')
            page_py = wiki_wiki.page(td.get('href'))
            summary = page_py.summary[0:600000000000]
            print(summary)
        i+=1
    break

# cred = credentials.Certificate('A:\Projects\CollegeProjects\YogaPartner\serviceAccountKey.json')
# firebase_admin.initialize_app(cred)
# db = firestore.client()

# for doc in docs:
#     doc_ref = db.collection(u'yoga-asanas').document()
#     doc_ref.set(doc)