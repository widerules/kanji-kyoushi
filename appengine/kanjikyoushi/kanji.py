from data.kanji_entry import kanji_entry
from datetime import datetime
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
from util.paging import PagedQuery, PageLinks
import logging
import os

PAGESIZE = 25

class KanjiPage( webapp.RequestHandler ):
    def get( self ):

        template_values = {
            'user': users.get_current_user(),
            'admin': users.is_current_user_admin(),
            'date_string': datetime.now().ctime(),
            'url': users.create_logout_url( self.request.host_url ),
            'link_text': 'logout',
        }

        if self.request.get( 'kanji' ):

            kanji = kanji_entry.all().filter( "kanji =", self.request.get( 'kanji' ) ).get()
            template_values['kanji'] = kanji

            if not template_values['kanji']:
                template_values['message'] = 'no kanji found'

            next_page = 'template/kanji.html'

        else:

            # setup paged query
            kanji_query = PagedQuery( kanji_entry.all(), PAGESIZE )

            # retreive requested page number, defaulting to page 1
            bookmark = self.request.get( 'bookmark' )
            if bookmark:
                bookmark = int( bookmark )
            else:
                bookmark = 1
            logging.info( 'bookmark=' + str( bookmark ) )

            template_values['bookmark'] = str( bookmark )
            template_values['kanji_list'] = kanji_query.fetch_page( bookmark )
            template_values['page_links'] = PageLinks( 
            page = bookmark,
            page_count = kanji_query.page_count(),
            url_root = "/kanji",
            page_field = "bookmark",
            page_range = 10 ).get_links()

            next_page = 'template/kanjilist.html'

        path = os.path.join( os.path.dirname( __file__ ), next_page )
        self.response.out.write( template.render( path, template_values ) )

application = webapp.WSGIApplication( [( '/kanji', KanjiPage )], debug = True )

def main():
    run_wsgi_app( application )

if __name__ == '__main__':
    main()

