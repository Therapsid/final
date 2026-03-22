import os
import re

def format_java_code(text):
    # 1. Importları tek blok yap (aradaki boşlukları sil)
    text = re.sub(r'(import\s+.*;\n)\s*\n+(import\s+.*;)', r'\1\2', text)
    
    # 2. Sınıf tanımı öncesi ve sonrasındaki anlamsız boşlukları temizle
    # 'public class' vb. öncesindeki 'public \n\n class' gibi durumları düzelt
    text = re.sub(r'(public|private|protected|class|interface|enum)\s*\n+\s*(public|private|protected|class|interface|enum)', r'\1 \2', text)
    text = re.sub(r'(public|private|protected|class|interface|enum)\s*\n+\s*\{', r'\1 {', text)

    # 3. Metodlar ve alanlar arası boşluk
    lines = text.splitlines()
    formatted_lines = []
    
    for i in range(len(lines)):
        line = lines[i]
        curr_trimmed = line.strip()
        
        # Eğer satır boşsa ve bir önceki satır da boşsa, bu satırı atla (ardışık boş satır engelleme)
        if not curr_trimmed and formatted_lines and not formatted_lines[-1].strip():
            continue
            
        formatted_lines.append(line)
        
        if i < len(lines) - 1:
            nxt_trimmed = lines[i+1].strip()
            
            # '}' ile biten metodlardan sonra boş satır ekle
            if curr_trimmed == '}' and nxt_trimmed and nxt_trimmed != '}':
                if not nxt_trimmed.startswith('else') and not nxt_trimmed.startswith('catch') and not nxt_trimmed.startswith('finally'):
                    formatted_lines.append('')
            
            # Sınıf içi değişken tanımlarından sonra boş satır ekle (eğer sonraki satır metod veya anotasyonsa)
            if curr_trimmed.endswith(';') and nxt_trimmed.startswith('@') and line.startswith('    ') and not line.startswith('        '):
                formatted_lines.append('')

    text = "\n".join(formatted_lines)
    
    # Son temizlik
    text = re.sub(r'\n\s*\n\s*\n+', '\n\n', text)
    
    return text.strip() + "\n"

def process_files(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                filepath = os.path.join(root, file)
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                formatted_content = format_java_code(content)
                
                # Gereksiz çift boş satırları tekrar temizle
                formatted_content = re.sub(r'\n\s*\n\s*\n', '\n\n', formatted_content)
                
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(formatted_content)

if __name__ == "__main__":
    process_files('backend/src/main/java')
