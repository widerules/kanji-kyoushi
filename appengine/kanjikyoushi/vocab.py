from data.kanji_entry import kanji_entry
from data.vocabulary_word import vocabulary_word
from datetime import datetime
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
from util.paging import PagedQuery, PageLinks
import logging
import os

PAGESIZE = 25

class VocabPage( webapp.RequestHandler ):
    def get( self ):

        template_values = {
            'user': users.get_current_user(),
            'admin': users.is_current_user_admin(),
            'date_string': datetime.now().ctime(),
            'url': users.create_logout_url( self.request.host_url ),
            'link_text': 'logout',
        }

        if self.request.get( "word" ):
            word = vocabulary_word.all().filter( "word =", self.request.get( "word" ) ).get()
            template_values['word'] = word

            template_values['related_kanji_list'] = []
            for entry in kanji_entry.get( word.related_kanji ):
                template_values['related_kanji_list'].append( entry.kanji )

            if not template_values['word']:
                template_values['message'] = 'no word found'

            next_page = 'template/vocab.html'

        else:
            search_string = self.request.get( 'search_string' )

            # setup paged query
            if search_string:
                template_values['search_string'] = search_string
                vocab_query = PagedQuery( vocabulary_word.all().order( 'modified' ).filter( 'word =', search_string ), PAGESIZE )
            else:
                vocab_query = PagedQuery( vocabulary_word.all().order( 'modified' ), PAGESIZE )

            # retreive requested page number, defaulting to page 1
            bookmark = self.request.get( 'bookmark' )
            if bookmark:
                bookmark = int( bookmark )
            else:
                bookmark = 1
            logging.info( 'bookmark=' + str( bookmark ) )

            template_values['bookmark'] = str( bookmark )
            template_values['vocab_list'] = vocab_query.fetch_page( bookmark )
            template_values['page_links'] = PageLinks( 
                page = bookmark,
                page_count = vocab_query.page_count(),
                url_root = "/vocab",
                page_field = "bookmark",
                page_range = 10 ).get_links()

            next_page = 'template/vocablist.html'

        path = os.path.join( os.path.dirname( __file__ ), next_page )
        self.response.out.write( template.render( path, template_values ) )

application = webapp.WSGIApplication( [( '/vocab', VocabPage )], debug = True )

def main():
    run_wsgi_app( application )

if __name__ == '__main__':
    main()

