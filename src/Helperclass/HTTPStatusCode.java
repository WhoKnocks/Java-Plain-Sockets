package Helperclass;

/**
 * Created by GJ on 25/02/2015.
 */
public enum HTTPStatusCode {

    OK {
        @Override
        public String toString() {
            return "200 OK";
        }

        @Override
        public String getResponse() {
            return "HTTP/1.1 200 OK";
        }
    },
    NOT_FOUND {
        @Override
        public String toString() {
            return "404 Not Found";
        }

        @Override
        public String getResponse() {
            return "HTTP/1.1 404 NOT_FOUND";
        }
    },
    SERVER_ERROR {
        @Override
        public String toString() {
            return "500 Server Error";
        }

        @Override
        public String getResponse() {
            return "HTTP/1.1 500 SERVER_ERROR";
        }
    },
    NOT_MODIFIED {
        @Override
        public String toString() {
            return "304 Not Modified";
        }

        @Override
        public String getResponse() {
            return "HTTP/1.1 304 NOT_MODIFIED";
        }

    };

    public abstract String toString();

    public abstract String getResponse();


}
