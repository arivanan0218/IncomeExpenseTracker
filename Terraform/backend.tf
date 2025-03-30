terraform {
  backend "s3" {
    bucket = "income-expense-terraform-statefile"
    key = "server_name/statefile"
    region = "ap-south-1"
  }
}