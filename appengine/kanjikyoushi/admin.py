import logging
import os

from data.vocabulary_word import vocabulary_word
from data.system_data import system_data, get_system_data

from datetime import datetime
from util.paging import PagedQuery, PageLinks

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

class MainPage(webapp.RequestHandler):
    def get(self):

        template_values = {
            'user': users.get_current_user(),
            'admin': users.is_current_user_admin(),
            'date_string': datetime.now().ctime(),
            'url': users.create_logout_url(self.request.host_url),
            'link_text': 'logout',
        }
       
        next_page = 'template/admin.html'
            
        path = os.path.join(os.path.dirname(__file__), next_page)
        self.response.out.write(template.render(path, template_values))

application = webapp.WSGIApplication([('/admin', MainPage)], debug=True)

def main():
    run_wsgi_app(application)
    
if __name__ == '__main__':
    main()

