import logging
from google.appengine.ext import db

class system_data(db.Model):
    min_index = db.IntegerProperty(default=1)
    max_index = db.IntegerProperty(default=1)
    word_count = db.IntegerProperty(default=0)

###########################################################################
# retrieve system data from datastore                                     #
# if no record exists in datastore, a new record is created and saved to  #
# the datastore and passed back.                                          #
###########################################################################
def get_system_data():
    data_query = system_data.all()
    data = data_query.get()
        
    if data:
        logging.debug('getting existing system data instance')
    else:
        logging.debug('creating new system data instance')
        data = system_data()
        data.put()
         
    return data

