from data.kanji_entry import kanji_entry
from datetime import datetime
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
import logging
import os
import time

class KanjiFeedPage( webapp.RequestHandler ):
    def get( self ):
        # returns all kanji in datastore

        kanji_list = []

        template_values = {
            'timestamp': time.mktime( datetime.utcnow().timetuple() ),
            'count': 0,
        }

        kanji_query = kanji_entry.all()
        for kanji in kanji_query:
            kanji_list.append( kanji )

        if kanji_list:
            template_values['kanji_list'] = kanji_list
            template_values['count'] = len( kanji_list )

        next_page = 'template/kanjifeed.xml'

        path = os.path.join( os.path.dirname( __file__ ), next_page )
        self.response.out.write( template.render( path, template_values ) )

    def post( self ):
        # add kanji to datastore

        template_values = {
            'timestamp': time.mktime( datetime.utcnow().timetuple() ),
            'count': 0,
        }

        kanji_list = []
        existing_kanji = []
        kanji_query = kanji_entry.all()
        for kanji in kanji_query:
            existing_kanji.append( kanji.kanji )

        # kanji_list is a set of unique strings
        new_kanji = self.request.get( 'kanji' )
        new_index = self.request.get( 'index' )
        new_kanji = new_kanji.strip()

        if new_kanji in existing_kanji:
            message = "%s already in kanji list" % ( new_kanji )

        elif new_kanji and not new_kanji in existing_kanji:
            new_entry = kanji_entry( kanji = new_kanji )
            if new_index:
                new_entry.index = int( new_index )
            new_entry = kanji_entry.get( new_entry.put() )
            message = "%s added to kanji list" % ( new_kanji )

            kanji_list.append( new_entry )

        else:
            message = "%s NOT added to kanji list" % ( new_kanji )

        logging.info( message )
        template_values['message'] = message

        if kanji_list:
            template_values['kanji_list'] = kanji_list
            template_values['count'] = len( kanji_list )

        next_page = 'template/kanjifeed.xml'
        path = os.path.join( os.path.dirname( __file__ ), next_page )
        self.response.out.write( template.render( path, template_values ) )

application = webapp.WSGIApplication( [( '/kanjifeed', KanjiFeedPage )], debug = True )

def main():
    run_wsgi_app( application )

if __name__ == '__main__':
    main()

