from data.kanji_entry import kanji_entry
from data.system_data import get_system_data
from data.vocabulary_word import vocabulary_word
from datetime import datetime
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
import logging
import os
import time

class VocabFeedPage( webapp.RequestHandler ):
    def get( self ):
        # supported actions: get, getkanji, update, validate

        words = []

        template_values = {
            'timestamp': time.mktime( datetime.utcnow().timetuple() ),
            'count': 0,
        }

        word_query = vocabulary_word.all()
        for word in word_query:
            words.append( word )

        if words:
            template_values['words'] = words
            template_values['count'] = len( words )

        next_page = 'template/vocabfeed.xml'

        path = os.path.join( os.path.dirname( __file__ ), next_page )
        self.response.out.write( template.render( path, template_values ) )

    def post( self ):

        new_words = []
        existing_words = []
        data = get_system_data()

        template_values = {
            'timestamp': time.mktime( datetime.utcnow().timetuple() ),
            'count': 0,
        }

        word_query = vocabulary_word.all()
        for word in word_query:
            existing_words.append( word )

        new_word = self.request.get( 'word' )

        if new_word:
            # retrieve existing record if any or create new record
            vocab_query = vocabulary_word.all()
            vocab_query.filter( "word =", new_word )
            vocab = vocab_query.get()

            is_new_word = False

            if not vocab:
                vocab = vocabulary_word( word = new_word )
                is_new_word = True

            # insert any new readings and meanings
            new_readings = self.request.get_all( 'reading' )
            for new_reading in new_readings:
                new_reading = new_reading.strip()
                if new_reading and not new_reading in vocab.readings:
                    vocab.readings.append( new_reading )

            new_meanings = self.request.get_all( 'meaning' )
            for new_meaning in new_meanings:
                new_meaning = new_meaning.strip()
                if new_meaning and not new_meaning in vocab.meanings:
                    vocab.meanings.append( new_meaning )

            # link related kanji
            for related_kanji in self.request.get_all( 'kanji' ):
                logging.info( "adding kanji '%s'" % related_kanji )
                try:
                    kanji_query = kanji_entry.all()
                    kanji_query.filter( "kanji =", related_kanji )
                    related_kanji_entry = kanji_query.get()
                    if related_kanji_entry:
                        vocab.related_kanji.append( related_kanji_entry.key() )

                    if related_kanji_entry.kanji_index > vocab.max_kanji_index:
                        vocab.max_kanji_index = related_kanji_entry.kanji_index

                except:
                    logging.error( "failed to add related kanji %s" % related_kanji )

            vocab = vocabulary_word.get( vocab.put() )

            if is_new_word:
                data.word_count = data.word_count + 1
                data.put()

            message = "new word '%s' created" % ( vocab.word )

            new_words.append( vocab )

        template_values['message'] = message

        if new_words:
            template_values['words'] = new_words
            template_values['count'] = len( new_words )

        next_page = 'template/vocabfeed.xml'

        path = os.path.join( os.path.dirname( __file__ ), next_page )
        self.response.out.write( template.render( path, template_values ) )

application = webapp.WSGIApplication( [( '/vocabfeed', VocabFeedPage )], debug = True )

def main():
    run_wsgi_app( application )

if __name__ == '__main__':
    main()

