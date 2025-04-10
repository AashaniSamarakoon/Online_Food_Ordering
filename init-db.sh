# #!/bin/bash
# set -e

# psql -v ON_ERROR_STOP=1 --username "postgres" <<-EOSQL
#     CREATE DATABASE driver_db;
#     CREATE DATABASE order_assignment_db;
    
#     GRANT ALL PRIVILEGES ON DATABASE driver_db TO postgres;
#     GRANT ALL PRIVILEGES ON DATABASE order_assignment_db TO postgres;
# EOSQL