-- Static Javascript and Coffeescript "import" directive 
module JSImport where 

import Control.Monad
import Text.Regex.Posix
import System.IO (readFile, writeFile)
import System.Environment 
import qualified Data.List as L
import Data.Set (Set)
import qualified Data.Set as S

type Filename = String

main :: IO ()
main = do 
    file  <- (liftM (!! 0)) getArgs
    set   <- resolve file S.empty 
    files <- sequence $ map readFile $ S.toList set 
    let output = foldl (\x y -> x ++ "\n\n" ++ y) "" files
    putStrLn output 
       
-- TODO: Examine Complexity 
resolve :: Filename -> Set Filename -> IO (Set Filename)
resolve f s 
    | f `S.member` s = return s
    | otherwise      = readFile f >>= \contents ->
        let rootPath = getPath f
            imps = map (rootPath ++) $ parseImports contents in
            (liftM (S.insert f)) (foldl (\s' e' -> s' >>= \s'' -> resolve e' s'') (return s) imps)

-- Haskell Regex is awesome 
parseImports  :: String -> [Filename]
parseImports  = map extractFileName . filter goodLine . map regex . filter (not . null) . lines
	where regex = (=~ "#import (\"|').*(\"|')") :: String -> String 
	      goodLine [] = False
	      goodLine (x:_) = x == '#'

extractFileName :: String -> Filename 
extractFileName s = case (L.findIndex (== '"') s) of 
                      Nothing -> error "Invalid Import Statement missing Filename in: " ++ s
                      Just i  -> removeQuotes . snd $ splitAt i s
    where removeQuotes = init . drop 1 
    
getPath :: Filename -> Filename 
getPath file = 
    let i = last $ L.findIndices (== '/') file in 
        fst $ splitAt (i + 1) file
