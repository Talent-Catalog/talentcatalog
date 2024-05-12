data "aws_route53_zone" "this" {
  name = var.site_domain
}

# Add an IPv4 DNS record pointing to the loab balancer
resource "aws_route53_record" "ipv4" {
  zone_id = data.aws_route53_zone.this.zone_id
  name    = var.site_domain
  type    = "A"

  alias {
    name                   = var.lb_dns_name
    zone_id                = var.lb_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "www" {
  zone_id = data.aws_route53_zone.this.zone_id
  name    = "www"
  type    = "CNAME"
  ttl     = "600"
  records = [var.site_domain]
}
