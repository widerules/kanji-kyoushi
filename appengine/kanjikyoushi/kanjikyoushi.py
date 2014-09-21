import os

from datetime import datetime

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

class MainPage(webapp.RequestHandler):
    def get(self):
        user = users.get_current_user()
        
        template_values = {
            'user': user,
            'admin' : users.is_current_user_admin(),
            'date_string': datetime.now().ctime(),
        }
       
        if user:
            template_values['url'] = users.create_logout_url(self.request.host_url)
            template_values['link_text'] = 'logout'
        else:
            template_values['url'] = users.create_login_url(self.request.host_url)
            template_values['link_text'] = 'login'
            
        next_page = 'template/kanjikyoushi.html'
            
        path = os.path.join(os.path.dirname(__file__), next_page)
        self.response.out.write(template.render(path, template_values))
        
application = webapp.WSGIApplication([('/', MainPage)], debug=True)

def main():
    run_wsgi_app(application)
    
if __name__ == '__main__':
    main()

