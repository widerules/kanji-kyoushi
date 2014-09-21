import logging
import os
import time

from data.vocabulary_word import vocabulary_word
from data.system_data import system_data, get_system_data

from datetime import datetime

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

class FeedPage(webapp.RequestHandler):
    def get(self):
        # supported actions: get, getkanji, update, validate
        
        action = self.request.get('action')
        logging.info('feed accessed, action: ' + action)

        words = []

        template_values = {
            'action': action,
            'timestamp': time.mktime(datetime.utcnow().timetuple()),
            'count': 0,
        }
       
        if not action:
            message = 'feed request missing parameter: \'action\''
            logging.error(message)
            template_values['message'] = message
            
        elif action == 'get':
                        
            try:
                kanji_index = int(self.request.get('kanji_index'))
            except:
                message = 'feed request missing or invalid parameter: kanji_index=%s' \
                    % (self.request.get('kanji_index'))
                logging.error(message)
                template_values['message'] = message
                kanji_index = None

            try:
                kanji_count = int(self.request.get('kanji_count'))
            except:
                message = 'feed request missing or invalid parameter: kanji_count=%s' \
                    % (self.request.get('kanji_count'))
                logging.error(message)
                template_values['message'] = message
                kanji_count = None
                    
            logging.info('action: %s, kanji_index: %s, kanji_count: %s' \
                % (action, kanji_index, kanji_count))
                    
            if kanji_index and kanji_count:
                word_query = vocabulary_word.all()
                word_query.order('kanji_index')
                word_query.filter('kanji_index >', kanji_index - 1)
                word_query.filter('kanji_index <', kanji_index + kanji_count)
                for word in word_query:
                    words.append(word)
                    
        elif action == 'update':

            try:
                kanji_index = int(self.request.get('kanji_index'))
            except:
                message = 'feed request missing or invalid parameter: kanji_index=%s' \
                    % (self.request.get('kanji_index'))
                logging.error(message)
                template_values['message'] = message
                kanji_index = None
            
            try:
                since = datetime.fromtimestamp(float(self.request.get('since')))
            except:
                message = 'feed request missing or invalid parameter: since=%s' \
                    % (self.request.get('since'))
                logging.error(message)
                template_values['message'] = message
                since = None

            logging.info('action: %s, id: %s, since: %s' % (action, kanji_index, since))

            if kanji_index and since:
                word_query = vocabulary_word.all()
                word_query.filter('modified >=', since)
                for word in word_query:
                    if word.kanji_index <= kanji_index:
                        words.append(word)

        elif action == 'validate':

            try:
                kanji_index = int(self.request.get('kanji_index'))
            except:
                message = 'feed request missing or invalid parameter: kanji_index=%s' \
                    % (self.request.get('kanji_index'))
                logging.error(message)
                template_values['message'] = message
                kanji_index = None

            logging.info('action: %s, id: %s' % (action, kanji_index))

            if kanji_index:
                word_query = vocabulary_word.all()
                word_query.order('kanji_index')
                word_query.filter('kanji_index <=', kanji_index)
                for word in word_query:
                    words.append(word)

        else:
            message = 'unsupported action request: ' + action
            logging.error(message)
            template_values['message'] = message

        if words:
            template_values['words'] = words
            template_values['count'] = len(words)
       
        next_page = 'template/feed.xml'
            
        path = os.path.join(os.path.dirname(__file__), next_page)
        self.response.out.write(template.render(path, template_values))

application = webapp.WSGIApplication([('/feed', FeedPage)], debug=True)

def main():
    run_wsgi_app(application)
    
if __name__ == '__main__':
    main()

