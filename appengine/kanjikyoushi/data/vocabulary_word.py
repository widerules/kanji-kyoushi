from google.appengine.ext import db

class vocabulary_word( db.Model ):
    word = db.StringProperty( required = True )
    readings = db.StringListProperty()
    meanings = db.StringListProperty()
    active = db.BooleanProperty( default = True )
    modified = db.DateTimeProperty( auto_now = True )
    related_kanji = db.ListProperty( db.Key )
    max_kanji_index = db.IntegerProperty( default = 0 )
