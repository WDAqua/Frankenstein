The script `generate-bash-scripts.py` is used to generate bash files for starting components to be used in various pipelines, given the different QA component pipelines in `all-components.csv`.
One script (in servers folder) will be run at each server. All processes start using nohup.

In order to stop nohup running on background:

   ```
   ps -ef | grep nohup
   kill -9 <PID>
   ```
