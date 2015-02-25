/**
 * Created by GJ on 25/02/2015.
 */
public enum HTTPStatusCode {

    OK {
        @Override
        public String toString() {
            return "200 OK";
        }
    },
    NOT_FOUND {
        @Override
        public String toString() {
            return "404 Not Found";
        }
    },
    SERVER_ERROR {
        @Override
        public String toString() {
            return "500 Server Error";
        }
    },
    NOT_MODIFIED {
        @Override
        public String toString() {
            return "304 Not Modified";
        }
    };

    public abstract String toString();


}
