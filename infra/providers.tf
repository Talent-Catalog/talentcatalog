# The main region for the website
provider "aws" {
  alias  = "main"
  region = "us-east-1" # N. Virginia
}

# The fallback region for the website (warm standby)
provider "aws" {
  alias  = "standby"
  region = "us-east-2" # Ohio
}
