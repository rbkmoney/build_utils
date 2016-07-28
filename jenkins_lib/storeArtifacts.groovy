def call(String artiFacts) {
  def archivedArtifacts = 'artifacts.tar.gz'
  sh "tar -czf ${archivedArtifacts} ${artiFacts}"
  step([$class: 'ArtifactArchiver', artifacts: archivedArtifacts, fingerprint: true])
}
