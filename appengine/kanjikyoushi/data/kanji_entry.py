from google.appengine.ext import db

class kanji_entry(db.Model):
    kanji = db.StringProperty(required=True)
    kanji_index = db.IntegerProperty()

