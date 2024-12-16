#  Copyright (c) 2024 Talent Catalog.
#
#  This program is free software: you can redistribute it and/or modify it under
#  the terms of the GNU Affero General Public License as published by the Free
#  Software Foundation, either version 3 of the License, or any later version.
#
#  This program is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
#  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
#  for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program. If not, see https://www.gnu.org/licenses/.
#
#  This program is free software: you can redistribute it and/or modify it under
#  the terms of the GNU Affero General Public License as published by the Free
#  Software Foundation, either version 3 of the License, or any later version.
#
#  This program is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
#  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
#  for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program. If not, see https://www.gnu.org/licenses/.

import boto3
import json
from datetime import datetime


def lambda_handler(event, context):
    # Initialize ECS client using Boto3 to interact with ECS service
    ecs_client = boto3.client('ecs')

    # Define the ECS cluster and service to be updated
    cluster_name = 'tctalent-test'
    service_name = 'tctalent-test'

    # Update the ECS service, forcing a new deployment to refresh the tasks
    response = ecs_client.update_service(
        cluster=cluster_name,
        service=service_name,
        forceNewDeployment=True
    )

    # Helper function to convert datetime objects into ISO format strings
    # Allows datetime values in the ECS response to be serializable into JSON
    def convert_datetime(obj):
        if isinstance(obj, datetime):
            return obj.isoformat()
        raise TypeError("Type not serializable")

    # Return the ECS service update response in a JSON-serializable format
    return {
        'statusCode': 200,
        'body': json.loads(json.dumps(response, default=convert_datetime))
    }
